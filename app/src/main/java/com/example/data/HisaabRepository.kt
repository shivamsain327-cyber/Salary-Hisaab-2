package com.example.data

import android.content.Context
import com.example.R
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.snapshots
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await

class HisaabRepository(
    private val db: FirebaseFirestore,
    private val auth: FirebaseAuth = Firebase.auth,
) {
    constructor(context: Context) : this(
        FirebaseFirestore.getInstance(
            context.applicationContext.getString(R.string.firestore_database_id)
        )
    )

    private fun requireUserId(): String {
        val uid = auth.currentUser?.uid
        if (uid.isNullOrBlank()) {
            throw IllegalStateException("User must be signed in with Google before accessing Firestore.")
        }
        return uid
    }

    private fun handleStreamError(
        error: Throwable,
        expectedUid: String,
        operationType: OperationType,
        path: String,
    ): Nothing {
        val firestoreError = generateSequence(error) { it.cause }
            .filterIsInstance<FirebaseFirestoreException>()
            .firstOrNull()
        if (firestoreError == null && error is CancellationException) throw error
        val exceptionToLog = firestoreError ?: (error as? Exception)
        if (exceptionToLog != null && auth.currentUser?.uid == expectedUid) {
            handleFirestoreError(exceptionToLog, operationType, path)
        }
        throw error
    }

    fun observeSettings(userId: String): Flow<UserLedgerSettings?> = flow {
        val uid = requireUserId()
        check(uid == userId) { "Signed-in user mismatch" }
        val path = "users/$uid"
        emitAll(
            db.collection("users").document(uid)
                .snapshots()
                .map { snapshot ->
                    if (snapshot.exists()) {
                        snapshot.toObject(
                            UserLedgerSettings::class.java,
                            DocumentSnapshot.ServerTimestampBehavior.ESTIMATE
                        )
                    } else null
                }
                .catch { error -> handleStreamError(error, uid, OperationType.GET, path) }
        )
    }

    fun observeSalaries(userId: String): Flow<List<MonthlySalary>> = flow {
        val uid = requireUserId()
        check(uid == userId) { "Signed-in user mismatch" }
        val path = "users/$uid/salaries"
        emitAll(
            db.collection("users").document(uid).collection("salaries")
                .snapshots()
                .map { snapshot ->
                    snapshot.toObjects(
                        MonthlySalary::class.java,
                        DocumentSnapshot.ServerTimestampBehavior.ESTIMATE
                    )
                }
                .catch { error -> handleStreamError(error, uid, OperationType.LIST, path) }
        )
    }

    fun observeTransactions(userId: String): Flow<List<LedgerTransaction>> = flow {
        val uid = requireUserId()
        check(uid == userId) { "Signed-in user mismatch" }
        val path = "users/$uid/transactions"
        emitAll(
            db.collection("users").document(uid).collection("transactions")
                .snapshots()
                .map { snapshot ->
                    snapshot.toObjects(
                        LedgerTransaction::class.java,
                        DocumentSnapshot.ServerTimestampBehavior.ESTIMATE
                    )
                }
                .catch { error -> handleStreamError(error, uid, OperationType.LIST, path) }
        )
    }

    fun observeLoans(userId: String): Flow<List<UdhaarLoan>> = flow {
        val uid = requireUserId()
        check(uid == userId) { "Signed-in user mismatch" }
        val path = "users/$uid/loans"
        emitAll(
            db.collection("users").document(uid).collection("loans")
                .snapshots()
                .map { snapshot ->
                    snapshot.toObjects(
                        UdhaarLoan::class.java,
                        DocumentSnapshot.ServerTimestampBehavior.ESTIMATE
                    )
                }
                .catch { error -> handleStreamError(error, uid, OperationType.LIST, path) }
        )
    }

    fun observeLoanPayments(userId: String): Flow<List<LoanPayment>> = flow {
        val uid = requireUserId()
        check(uid == userId) { "Signed-in user mismatch" }
        val path = "users/$uid/loanPayments"
        emitAll(
            db.collection("users").document(uid).collection("loanPayments")
                .snapshots()
                .map { snapshot ->
                    snapshot.toObjects(
                        LoanPayment::class.java,
                        DocumentSnapshot.ServerTimestampBehavior.ESTIMATE
                    )
                }
                .catch { error -> handleStreamError(error, uid, OperationType.LIST, path) }
        )
    }

    suspend fun setOpeningCash(openingCash: Double): Result<Unit> {
        var path = "users/unauthenticated"
        return try {
            val uid = requireUserId()
            require(openingCash >= 0.0 && openingCash <= 1_000_000_000.0) {
                "Opening cash must be between 0 and 1,000,000,000"
            }
            val docRef = db.collection("users").document(uid)
            path = docRef.path
            db.runTransaction { tx ->
                val snap = tx.get(docRef)
                if (snap.exists()) {
                    tx.update(
                        docRef,
                        mapOf(
                            "openingCash" to openingCash,
                            "updatedAt" to FieldValue.serverTimestamp(),
                        )
                    )
                } else {
                    tx.set(
                        docRef,
                        mapOf(
                            "userId" to uid,
                            "openingCash" to openingCash,
                            "createdAt" to FieldValue.serverTimestamp(),
                            "updatedAt" to FieldValue.serverTimestamp(),
                        )
                    )
                }
                null
            }.await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.WRITE, path)
            Result.failure(e)
        }
    }

    suspend fun setMonthlySalary(monthKey: String, amount: Double): Result<Unit> {
        var path = "users/unauthenticated/salaries/$monthKey"
        return try {
            val uid = requireUserId()
            require(MONTH_REGEX.matches(monthKey)) { "Invalid month format" }
            require(amount >= 0.0 && amount <= 1_000_000_000.0) {
                "Salary must be between 0 and 1,000,000,000"
            }
            val docRef = db.collection("users").document(uid).collection("salaries").document(monthKey)
            path = docRef.path
            db.runTransaction { tx ->
                val snap = tx.get(docRef)
                if (snap.exists()) {
                    tx.update(
                        docRef,
                        mapOf(
                            "amount" to amount,
                            "updatedAt" to FieldValue.serverTimestamp(),
                        )
                    )
                } else {
                    tx.set(
                        docRef,
                        mapOf(
                            "userId" to uid,
                            "monthKey" to monthKey,
                            "amount" to amount,
                            "createdAt" to FieldValue.serverTimestamp(),
                            "updatedAt" to FieldValue.serverTimestamp(),
                        )
                    )
                }
                null
            }.await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.WRITE, path)
            Result.failure(e)
        }
    }

    suspend fun addTransaction(
        type: TransactionType,
        amount: Double,
        category: String,
        note: String,
        date: String,
    ): Result<Unit> {
        var path = "users/unauthenticated/transactions"
        return try {
            val uid = requireUserId()
            require(amount > 0.0 && amount <= 1_000_000_000.0) { "Amount must be greater than 0" }
            val trimmedCat = category.trim()
            require(trimmedCat.isNotEmpty() && trimmedCat.length <= 40) { "Invalid category" }
            require(DATE_REGEX.matches(date)) { "Invalid date format" }
            val trimmedNote = note.trim().take(60)
            val monthKey = date.substring(0, 7)

            val docRef = db.collection("users").document(uid).collection("transactions").document()
            path = docRef.path
            val payload = mapOf(
                "userId" to uid,
                "type" to type.name,
                "amount" to amount,
                "category" to trimmedCat,
                "note" to trimmedNote.takeIf { it.isNotEmpty() },
                "date" to date,
                "monthKey" to monthKey,
                "createdAt" to FieldValue.serverTimestamp(),
                "updatedAt" to FieldValue.serverTimestamp(),
            ).filterValues { it != null }

            docRef.set(payload).await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, path)
            Result.failure(e)
        }
    }

    suspend fun deleteTransaction(transactionId: String): Result<Unit> {
        var path = "users/unauthenticated/transactions/$transactionId"
        return try {
            val uid = requireUserId()
            require(transactionId.isNotBlank()) { "Transaction ID must not be blank" }
            val docRef = db.collection("users").document(uid).collection("transactions").document(transactionId)
            path = docRef.path
            docRef.delete().await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.DELETE, path)
            Result.failure(e)
        }
    }

    suspend fun addLoan(
        person: String,
        type: LoanType,
        amount: Double,
        date: String,
        dueDate: String?,
        note: String,
    ): Result<Unit> {
        var path = "users/unauthenticated/loans"
        return try {
            val uid = requireUserId()
            val trimmedPerson = person.trim().take(40)
            require(trimmedPerson.isNotEmpty()) { "Person name cannot be empty" }
            require(amount > 0.0 && amount <= 1_000_000_000.0) { "Amount must be greater than 0" }
            require(DATE_REGEX.matches(date)) { "Invalid date format" }
            val cleanDue = dueDate?.trim()?.takeIf { it.isNotEmpty() }
            if (cleanDue != null) {
                require(DATE_REGEX.matches(cleanDue)) { "Invalid due date format" }
            }
            val cleanNote = note.trim().take(60).takeIf { it.isNotEmpty() }

            val docRef = db.collection("users").document(uid).collection("loans").document()
            path = docRef.path
            val payload = mapOf(
                "userId" to uid,
                "person" to trimmedPerson,
                "type" to type.name,
                "amount" to amount,
                "date" to date,
                "dueDate" to cleanDue,
                "note" to cleanNote,
                "createdAt" to FieldValue.serverTimestamp(),
                "updatedAt" to FieldValue.serverTimestamp(),
            ).filterValues { it != null }

            docRef.set(payload).await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, path)
            Result.failure(e)
        }
    }

    suspend fun addLoanPayment(
        loanId: String,
        amount: Double,
        date: String,
    ): Result<Unit> {
        var path = "users/unauthenticated/loanPayments"
        return try {
            val uid = requireUserId()
            require(loanId.isNotBlank()) { "Loan ID must not be blank" }
            require(amount > 0.0 && amount <= 1_000_000_000.0) { "Amount must be greater than 0" }
            require(DATE_REGEX.matches(date)) { "Invalid date format" }

            val docRef = db.collection("users").document(uid).collection("loanPayments").document()
            path = docRef.path
            val payload = mapOf(
                "userId" to uid,
                "loanId" to loanId,
                "amount" to amount,
                "date" to date,
                "createdAt" to FieldValue.serverTimestamp(),
                "updatedAt" to FieldValue.serverTimestamp(),
            )
            docRef.set(payload).await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, path)
            Result.failure(e)
        }
    }

    suspend fun deleteLoanAndPayments(loanId: String): Result<Unit> {
        var path = "users/unauthenticated/loans/$loanId"
        return try {
            val uid = requireUserId()
            require(loanId.isNotBlank()) { "Loan ID must not be blank" }
            val userDoc = db.collection("users").document(uid)
            val loanRef = userDoc.collection("loans").document(loanId)
            path = loanRef.path

            val paymentsSnap = userDoc.collection("loanPayments")
                .whereEqualTo("loanId", loanId)
                .get()
                .await()

            val batch = db.batch()
            for (doc in paymentsSnap.documents) {
                batch.delete(doc.reference)
            }
            batch.delete(loanRef)
            batch.commit().await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.DELETE, path)
            Result.failure(e)
        }
    }

    private companion object {
        val MONTH_REGEX = Regex("^[0-9]{4}-[0-9]{2}$")
        val DATE_REGEX = Regex("^[0-9]{4}-[0-9]{2}-[0-9]{2}$")
    }
}
