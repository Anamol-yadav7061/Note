const {
  initializeTestEnvironment,
  assertFails,
  assertSucceeds,
} = require("@firebase/rules-unit-testing");
const { test, before, after, beforeEach } = require("node:test");
const fs = require("node:fs");

let testEnv;
const PROJECT_ID = "demo-no-project";
const ALICE_UID = "alice_123";
const BOB_UID = "bob_456";

const [emulatorHost, emulatorPortStr] = (process.env.FIRESTORE_EMULATOR_HOST || "127.0.0.1:8085").split(":");
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

// 1. Unauthenticated checks
test("Unauthenticated user: cannot read or write notes", async () => {
  const unauthDb = testEnv.unauthenticatedContext().firestore();
  await assertFails(unauthDb.collection("users").doc(ALICE_UID).collection("notes").get());
  await assertFails(
    unauthDb.collection("users").doc(ALICE_UID).collection("notes").doc("note1").set({
      userId: ALICE_UID,
      title: "Test",
    })
  );
});

// 2. Cross-user access checks
test("Authenticated user: cannot read another user's notes", async () => {
  await testEnv.withSecurityRulesDisabled(async (context) => {
    await context.firestore().collection("users").doc(BOB_UID).collection("notes").doc("bob_note").set({
      userId: BOB_UID,
      title: "Bob's Secret Script",
      content: "print('hello')",
      category: "Python",
      tags: ["python"],
      colorTheme: "#00FF66",
      isVault: false,
      isPinned: false,
      isArchived: false,
      noteType: "python",
      createdAt: new Date(),
      updatedAt: new Date(),
    });
  });

  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  await assertFails(aliceDb.collection("users").doc(BOB_UID).collection("notes").doc("bob_note").get());
  await assertFails(aliceDb.collection("users").doc(BOB_UID).collection("notes").get());
});

// 3. User can create, read, and update own note
test("Authenticated user: can create and read their own note", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const noteRef = aliceDb.collection("users").doc(ALICE_UID).collection("notes").doc("alice_note_1");

  await assertSucceeds(
    noteRef.set({
      userId: ALICE_UID,
      title: "Anamol Script",
      content: "import sys\nprint('Anamol Notes')",
      category: "Python",
      tags: ["dev", "script"],
      colorTheme: "#00FF66",
      isVault: false,
      isPinned: true,
      isArchived: false,
      noteType: "python",
      createdAt: new Date(),
      updatedAt: new Date(),
    })
  );

  await assertSucceeds(noteRef.get());
  await assertSucceeds(aliceDb.collection("users").doc(ALICE_UID).collection("notes").get());
});

// 4. User can create and read Jarvis message
test("Authenticated user: can add and read Jarvis messages", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const msgRef = aliceDb.collection("users").doc(ALICE_UID).collection("jarvis_messages").doc("msg_1");

  await assertSucceeds(
    msgRef.set({
      userId: ALICE_UID,
      sender: "user",
      message: "Jarvis, write a python quicksort function",
      createdAt: new Date(),
    })
  );

  await assertSucceeds(msgRef.get());
});

// 5. User can create and read UserSettings with geminiApiKey
test("Authenticated user: can save and read UserSettings with geminiApiKey", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const settingsRef = aliceDb.collection("users").doc(ALICE_UID).collection("settings").doc("user_settings");

  await assertSucceeds(
    settingsRef.set({
      userId: ALICE_UID,
      fontFamily: "Fira Code",
      fontSize: "MEDIUM",
      terminalTheme: "MATRIX_GREEN",
      vaultPin: "1234",
      userName: "Anamol",
      geminiApiKey: "AIzaSyFakeKeyForTestingJarvisAI",
      createdAt: new Date(),
      updatedAt: new Date(),
    })
  );

  const snap = await assertSucceeds(settingsRef.get());
  const data = snap.data();
  if (data.geminiApiKey !== "AIzaSyFakeKeyForTestingJarvisAI") {
    throw new Error("geminiApiKey does not match expected value");
  }
});
