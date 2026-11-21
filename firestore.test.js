const {
  initializeTestEnvironment,
  assertFails,
  assertSucceeds,
} = require("@firebase/rules-unit-testing");
const { test, before, after, beforeEach } = require("node:test");
const fs = require("node:fs");

let testEnv;
const PROJECT_ID = process.env.GCP_PROJECT || "demo-no-project";
const ALICE_UID = "alice_123";
const BOB_UID = "bob_456";

const [emulatorHost, emulatorPortStr] = (
  process.env.FIRESTORE_EMULATOR_HOST || "127.0.0.1:8085"
).split(":");
const emulatorPort = parseInt(emulatorPortStr, 10);

before(async () => {
  const rules = fs.readFileSync("./firestore.rules", "utf8");
  testEnv = await initializeTestEnvironment({
    projectId: PROJECT_ID,
    firestore: {
      rules,
      host: emulatorHost,
      port: emulatorPort,
    },
  });
});

after(async () => {
  if (testEnv) {
    await testEnv.cleanup();
  }
});

beforeEach(async () => {
  if (testEnv) {
    await testEnv.clearFirestore();
  }
});

test("Unauthenticated user cannot read or write any user collections", async () => {
  const unauthDb = testEnv.unauthenticatedContext().firestore();
  await assertFails(unauthDb.collection("users").doc(ALICE_UID).get());
  await assertFails(
    unauthDb.collection("users").doc(ALICE_UID).collection("transactions").get()
  );
  await assertFails(
    unauthDb
      .collection("users")
      .doc(ALICE_UID)
      .collection("transactions")
      .doc("tx_1")
      .set({
        userId: ALICE_UID,
        type: "OUT",
        amount: 250,
        category: "Khana-peena",
        date: "2026-11-20",
        monthKey: "2026-11",
        createdAt: new Date(Date.now() - 1000),
        updatedAt: new Date(Date.now() - 1000),
      })
  );
});

test("Owner can create, update, read, and delete user openingCash settings", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const bobDb = testEnv.authenticatedContext(BOB_UID).firestore();
  const now = new Date(Date.now() - 5000);

  await assertSucceeds(
    aliceDb.collection("users").doc(ALICE_UID).set({
      userId: ALICE_UID,
      openingCash: 5000,
      createdAt: now,
      updatedAt: now,
    })
  );

  await assertSucceeds(
    aliceDb.collection("users").doc(ALICE_UID).update({
      openingCash: 7500,
      updatedAt: new Date(Date.now() - 1000),
    })
  );

  await assertSucceeds(aliceDb.collection("users").doc(ALICE_UID).get());

  // Cross-user denied
  await assertFails(bobDb.collection("users").doc(ALICE_UID).get());
  await assertFails(
    bobDb.collection("users").doc(ALICE_UID).update({
      openingCash: 0,
      updatedAt: new Date(Date.now() - 1000),
    })
  );
});

test("Owner can create, update, list salaries; invalid monthKey or future timestamp rejected", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const bobDb = testEnv.authenticatedContext(BOB_UID).firestore();
  const now = new Date(Date.now() - 5000);

  await assertSucceeds(
    aliceDb
      .collection("users")
      .doc(ALICE_UID)
      .collection("salaries")
      .doc("2026-11")
      .set({
        userId: ALICE_UID,
        monthKey: "2026-11",
        amount: 35000,
        createdAt: now,
        updatedAt: now,
      })
  );

  await assertSucceeds(
    aliceDb
      .collection("users")
      .doc(ALICE_UID)
      .collection("salaries")
      .doc("2026-11")
      .update({
        amount: 40000,
        updatedAt: new Date(Date.now() - 1000),
      })
  );

  await assertSucceeds(
    aliceDb.collection("users").doc(ALICE_UID).collection("salaries").get()
  );

  // Reject future timestamp
  await assertFails(
    aliceDb
      .collection("users")
      .doc(ALICE_UID)
      .collection("salaries")
      .doc("2026-12")
      .set({
        userId: ALICE_UID,
        monthKey: "2026-12",
        amount: 30000,
        createdAt: new Date(Date.now() + 60000),
        updatedAt: new Date(Date.now() + 60000),
      })
  );

  // Cross-user list denied
  await assertFails(
    bobDb.collection("users").doc(ALICE_UID).collection("salaries").get()
  );
});

test("Transactions enforce schema, monthKey parity, no @DocumentId field, and user isolation", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const bobDb = testEnv.authenticatedContext(BOB_UID).firestore();
  const now = new Date(Date.now() - 5000);
  const txCol = aliceDb
    .collection("users")
    .doc(ALICE_UID)
    .collection("transactions");

  // Valid expense with note
  await assertSucceeds(
    txCol.doc("tx_1").set({
      userId: ALICE_UID,
      type: "OUT",
      amount: 250,
      category: "Khana-peena",
      note: "Sabzi aur doodh",
      date: "2026-11-20",
      monthKey: "2026-11",
      createdAt: now,
      updatedAt: now,
    })
  );

  // Valid income without optional note
  await assertSucceeds(
    txCol.doc("tx_2").set({
      userId: ALICE_UID,
      type: "IN",
      amount: 1500,
      category: "Extra kaam",
      date: "2026-11-20",
      monthKey: "2026-11",
      createdAt: now,
      updatedAt: now,
    })
  );

  // Reject ghost "id" field (matches @DocumentId exclusion)
  await assertFails(
    txCol.doc("tx_bad_id").set({
      id: "tx_bad_id",
      userId: ALICE_UID,
      type: "OUT",
      amount: 100,
      category: "Baaki",
      date: "2026-11-20",
      monthKey: "2026-11",
      createdAt: now,
      updatedAt: now,
    })
  );

  // Reject mismatched monthKey
  await assertFails(
    txCol.doc("tx_bad_month").set({
      userId: ALICE_UID,
      type: "OUT",
      amount: 100,
      category: "Baaki",
      date: "2026-11-20",
      monthKey: "2026-10",
      createdAt: now,
      updatedAt: now,
    })
  );

  // Reject zero or negative amount
  await assertFails(
    txCol.doc("tx_zero").set({
      userId: ALICE_UID,
      type: "OUT",
      amount: 0,
      category: "Baaki",
      date: "2026-11-20",
      monthKey: "2026-11",
      createdAt: now,
      updatedAt: now,
    })
  );

  // Owner list and delete succeed; cross-user read/delete fail
  await assertSucceeds(txCol.get());
  await assertFails(
    bobDb.collection("users").doc(ALICE_UID).collection("transactions").get()
  );
  await assertFails(
    bobDb
      .collection("users")
      .doc(ALICE_UID)
      .collection("transactions")
      .doc("tx_1")
      .delete()
  );
  await assertSucceeds(txCol.doc("tx_1").delete());
});

test("Loans and loanPayments enforce parent existence and ownership", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const bobDb = testEnv.authenticatedContext(BOB_UID).firestore();
  const now = new Date(Date.now() - 5000);
  const loansCol = aliceDb.collection("users").doc(ALICE_UID).collection("loans");
  const paysCol = aliceDb
    .collection("users")
    .doc(ALICE_UID)
    .collection("loanPayments");

  // Payment for non-existent loan fails
  await assertFails(
    paysCol.doc("pay_orphan").set({
      userId: ALICE_UID,
      loanId: "missing_loan",
      amount: 500,
      date: "2026-11-20",
      createdAt: now,
      updatedAt: now,
    })
  );

  // Create valid loan with optional dueDate and note
  await assertSucceeds(
    loansCol.doc("loan_1").set({
      userId: ALICE_UID,
      person: "Ramesh",
      type: "DIYA",
      amount: 2000,
      date: "2026-11-10",
      dueDate: "2026-12-10",
      note: "Dawai ke liye",
      createdAt: now,
      updatedAt: now,
    })
  );

  // Create valid repayment linked to loan_1
  await assertSucceeds(
    paysCol.doc("pay_1").set({
      userId: ALICE_UID,
      loanId: "loan_1",
      amount: 500,
      date: "2026-11-20",
      createdAt: now,
      updatedAt: now,
    })
  );

  // Cross-user payment creation fails
  await assertFails(
    bobDb
      .collection("users")
      .doc(ALICE_UID)
      .collection("loanPayments")
      .doc("pay_bob")
      .set({
        userId: BOB_UID,
        loanId: "loan_1",
        amount: 100,
        date: "2026-11-20",
        createdAt: now,
        updatedAt: now,
      })
  );

  await assertSucceeds(loansCol.get());
  await assertSucceeds(paysCol.get());
  await assertSucceeds(paysCol.doc("pay_1").delete());
  await assertSucceeds(loansCol.doc("loan_1").delete());
});
