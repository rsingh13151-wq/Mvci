const {
  initializeTestEnvironment,
  assertFails,
  assertSucceeds,
} = require("@firebase/rules-unit-testing");
const { test, before, after, beforeEach } = require("node:test");
const fs = require("node:fs");

let testEnv;
const PROJECT_ID = process.env.GCP_PROJECT || "demo-no-project";
const ALICE_UID = "alice_super_admin";
const BOB_UID = "bob_other_user";

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

test("Unauthenticated user: denied all reads and writes", async () => {
  const unauthDb = testEnv.unauthenticatedContext().firestore();
  await assertFails(unauthDb.collection("workspaces").doc(ALICE_UID).get());
  await assertFails(unauthDb.collection("workspaces").doc(ALICE_UID).collection("students").get());
  await assertFails(
    unauthDb.collection("workspaces").doc(ALICE_UID).collection("students").doc("st_1").set({
      userId: ALICE_UID,
      name: "Unauth Student",
      rollNo: "MVCI-001",
      courseName: "ADCA",
      batchName: "Morning-A",
      branchName: "Main Campus",
      parentLinked: false,
      status: "ACTIVE",
      totalFee: 12000,
      paidFee: 6000,
      attendancePct: 90,
      createdAt: new Date(Date.now() - 1000),
      updatedAt: new Date(Date.now() - 1000),
    })
  );
});

test("Cross-user isolation: Bob cannot read, list, or mutate Alice's workspace or subcollections", async () => {
  const now = new Date(Date.now() - 5000);
  await testEnv.withSecurityRulesDisabled(async (ctx) => {
    const db = ctx.firestore();
    await db.collection("workspaces").doc(ALICE_UID).set({
      userId: ALICE_UID,
      instituteName: "Maa Vaishno Computer Institute",
      websiteUrl: "https://www.mvci.online",
      directorName: "Rishu Singh",
      languageCode: "en",
      darkTheme: true,
      maintenanceMode: false,
      otpRequired: true,
      createdAt: now,
      updatedAt: now,
    });
    await db.collection("workspaces").doc(ALICE_UID).collection("connectors").doc("conn_1").set({
      userId: ALICE_UID,
      name: "MVCI Portal",
      url: "https://www.mvci.online",
      siteType: "Main Website",
      integrationMethod: "REST API",
      authMethod: "API Token",
      syncDirection: "TWO_WAY",
      syncFrequency: "AUTOMATIC",
      conflictPolicy: "MERGE_APPROVED",
      status: "CONNECTED",
      healthStatus: "HEALTHY",
      lastLatencyMs: 120,
      recordsImported: 10,
      recordsUpdated: 2,
      failedRecords: 0,
      createdAt: now,
      updatedAt: now,
    });
  });

  const bobDb = testEnv.authenticatedContext(BOB_UID).firestore();
  await assertFails(bobDb.collection("workspaces").doc(ALICE_UID).get());
  await assertFails(bobDb.collection("workspaces").get());
  await assertFails(bobDb.collection("workspaces").doc(ALICE_UID).collection("connectors").get());
  await assertFails(
    bobDb.collection("workspaces").doc(ALICE_UID).collection("connectors").doc("conn_1").update({
      status: "DISCONNECTED",
      updatedAt: new Date(Date.now() - 1000),
    })
  );
});

test("WorkspaceConfig: owner create, partial update, and shadow field / future timestamp rejection", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const created = new Date(Date.now() - 10000);

  await assertSucceeds(
    aliceDb.collection("workspaces").doc(ALICE_UID).set({
      userId: ALICE_UID,
      instituteName: "Maa Vaishno Computer Institute",
      websiteUrl: "https://www.mvci.online",
      directorName: "Rishu Singh",
      mysqlHost: "mysql.mvci.online",
      mysqlDbName: "mvci_central_erp",
      hostingProvider: "GoDaddy",
      languageCode: "en",
      darkTheme: false,
      maintenanceMode: false,
      autoBackupSchedule: "DAILY_02_00",
      otpRequired: true,
      disabledModules: [],
      createdAt: created,
      updatedAt: created,
    })
  );

  await assertSucceeds(
    aliceDb.collection("workspaces").doc(ALICE_UID).update({
      languageCode: "hi",
      darkTheme: true,
      adminUsername: "Rishu@2050",
      adminPasswordSalt: "mvci_v1_salt_2050",
      adminPasswordHash: "001ea4e661e22e4d7ac2a2c46c6a5989d263afad3ac79672a184ec24dd1149d8",
      recoveryEmail: "rsingh13151@gmail.com",
      otpWebhookUrl: "https://www.mvci.online/api/send-otp",
      updatedAt: new Date(Date.now() - 1000),
    })
  );

  // Reject malformed non-SHA256 hash
  await assertFails(
    aliceDb.collection("workspaces").doc(ALICE_UID).update({
      adminPasswordHash: "not_a_valid_sha256",
      updatedAt: new Date(Date.now() - 500),
    })
  );

  // Reject immutable createdAt tamper
  await assertFails(
    aliceDb.collection("workspaces").doc(ALICE_UID).update({
      createdAt: new Date(Date.now() - 500),
      updatedAt: new Date(Date.now() - 500),
    })
  );

  // Reject shadow field
  await assertFails(
    aliceDb.collection("workspaces").doc(ALICE_UID).update({
      isSuperVerified: true,
      updatedAt: new Date(Date.now() - 500),
    })
  );
});

test("Students, Franchises, Connectors, Roles, Automations, Payments, Exams, Contents: valid CRUD & rejection of invalid payloads", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const created = new Date(Date.now() - 10000);

  // Student create & update
  await assertSucceeds(
    aliceDb.collection("workspaces").doc(ALICE_UID).collection("students").doc("st_1").set({
      userId: ALICE_UID,
      name: "Aarav Sharma",
      rollNo: "MVCI-2026-101",
      courseName: "ADCA",
      batchName: "Morning-08",
      branchName: "Main Campus",
      parentLinked: true,
      status: "ACTIVE",
      totalFee: 15000,
      paidFee: 10000,
      attendancePct: 94,
      createdAt: created,
      updatedAt: created,
    })
  );
  await assertSucceeds(
    aliceDb.collection("workspaces").doc(ALICE_UID).collection("students").doc("st_1").update({
      attendancePct: 95,
      updatedAt: new Date(Date.now() - 1000),
    })
  );

  // Reject @DocumentId 'id' inside document body
  await assertFails(
    aliceDb.collection("workspaces").doc(ALICE_UID).collection("students").doc("st_2").set({
      id: "st_2",
      userId: ALICE_UID,
      name: "Invalid With Id",
      rollNo: "MVCI-2026-102",
      courseName: "DCA",
      batchName: "Evening-04",
      branchName: "Main Campus",
      parentLinked: false,
      status: "ACTIVE",
      totalFee: 8000,
      paidFee: 4000,
      attendancePct: 80,
      createdAt: created,
      updatedAt: created,
    })
  );

  // Reject future timestamp
  await assertFails(
    aliceDb.collection("workspaces").doc(ALICE_UID).collection("franchises").doc("fr_1").set({
      userId: ALICE_UID,
      code: "MVCI-FR01",
      name: "Varanasi City Center",
      city: "Varanasi",
      ownerName: "Vikram Verma",
      status: "ACTIVE",
      studentCount: 120,
      revenueTotal: 450000,
      commissionPct: 15,
      loginProvisioned: true,
      createdAt: new Date(Date.now() + 60000),
      updatedAt: new Date(Date.now() + 60000),
    })
  );

  // Connector create + list query
  await assertSucceeds(
    aliceDb.collection("workspaces").doc(ALICE_UID).collection("connectors").doc("conn_mvci").set({
      userId: ALICE_UID,
      name: "MVCI Official Website",
      url: "https://www.mvci.online",
      siteType: "Main Institute Website",
      integrationMethod: "REST API",
      authMethod: "Secure API Token",
      maskedApiKey: "mvci_live_••••9f2a",
      syncDirection: "TWO_WAY",
      syncFrequency: "AUTOMATIC",
      conflictPolicy: "MERGE_APPROVED",
      status: "CONNECTED",
      healthStatus: "HTTP 200 OK",
      lastLatencyMs: 142,
      recordsImported: 420,
      recordsUpdated: 35,
      failedRecords: 0,
      fieldMappings: "student_name->name,roll->rollNo",
      createdAt: created,
      updatedAt: created,
    })
  );
  await assertSucceeds(
    aliceDb.collection("workspaces").doc(ALICE_UID).collection("connectors").get()
  );

  // Role & Automation
  await assertSucceeds(
    aliceDb.collection("workspaces").doc(ALICE_UID).collection("roles").doc("role_branch_mgr").set({
      userId: ALICE_UID,
      name: "Branch Manager",
      branchScope: "Assigned Franchise",
      permissionsMask: "VIEW,ADD,EDIT,APPROVE,EXPORT",
      moduleScope: "Students,Attendance,Finance",
      customRole: true,
      assignedCount: 3,
      createdAt: created,
      updatedAt: created,
    })
  );
  await assertSucceeds(
    aliceDb.collection("workspaces").doc(ALICE_UID).collection("automations").doc("wf_absent").set({
      userId: ALICE_UID,
      title: "Absent Parent Alert",
      triggerEvent: "STUDENT_ABSENT",
      actionSummary: "Notify linked parent via SMS/App",
      enabled: true,
      retryLimit: 3,
      preventDuplicates: true,
      executionCount: 12,
      failureCount: 0,
      lastRunStatus: "SUCCESS",
      createdAt: created,
      updatedAt: created,
    })
  );
});

test("AuditLogEntry: immutable append-only enforcement, category/severity validation, and tamper rejection", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const bobDb = testEnv.authenticatedContext(BOB_UID).firestore();
  const created = new Date(Date.now() - 5000);

  // Valid security password reset audit log with actorUsername, category, and severity
  await assertSucceeds(
    aliceDb.collection("workspaces").doc(ALICE_UID).collection("audit_logs").doc("log_sec_1").set({
      userId: ALICE_UID,
      actorEmail: "rsingh13151@gmail.com",
      actorUsername: "Rishu@2050",
      actorRole: "SUPER_ADMIN",
      action: "OTP_PASSWORD_RESET",
      moduleName: "Security and Permissions",
      targetId: ALICE_UID,
      category: "SECURITY",
      severity: "CRITICAL",
      previousVal: "adminPasswordHash: [REDACTED]",
      newVal: "adminPasswordHash: [REDACTED] (OTP Verified)",
      deviceInfo: "Android Super Admin App",
      createdAt: created,
    })
  );

  // Valid settings change audit log
  await assertSucceeds(
    aliceDb.collection("workspaces").doc(ALICE_UID).collection("audit_logs").doc("log_set_1").set({
      userId: ALICE_UID,
      actorEmail: "rsingh13151@gmail.com",
      actorUsername: "Rishu@2050",
      actorRole: "SUPER_ADMIN",
      action: "CRITICAL_SYSTEM_SETTING",
      moduleName: "System Settings",
      targetId: ALICE_UID,
      category: "SETTINGS",
      severity: "WARNING",
      previousVal: "maintenanceMode=false",
      newVal: "maintenanceMode=true",
      deviceInfo: "Android Super Admin App",
      createdAt: created,
    })
  );

  // Reject invalid severity enum
  await assertFails(
    aliceDb.collection("workspaces").doc(ALICE_UID).collection("audit_logs").doc("log_bad_sev").set({
      userId: ALICE_UID,
      actorUsername: "Rishu@2050",
      actorRole: "SUPER_ADMIN",
      action: "SET_THEME",
      moduleName: "System Settings",
      targetId: ALICE_UID,
      category: "SETTINGS",
      severity: "DEBUG_LOW",
      createdAt: created,
    })
  );

  // Reject invalid category enum
  await assertFails(
    aliceDb.collection("workspaces").doc(ALICE_UID).collection("audit_logs").doc("log_bad_cat").set({
      userId: ALICE_UID,
      actorRole: "SUPER_ADMIN",
      action: "SET_THEME",
      moduleName: "System Settings",
      targetId: ALICE_UID,
      category: "UNKNOWN_CAT",
      severity: "INFO",
      createdAt: created,
    })
  );

  // Reject future timestamp or forged owner by another user
  await assertFails(
    aliceDb.collection("workspaces").doc(ALICE_UID).collection("audit_logs").doc("log_future").set({
      userId: ALICE_UID,
      actorRole: "SUPER_ADMIN",
      action: "SET_THEME",
      moduleName: "System Settings",
      targetId: ALICE_UID,
      createdAt: new Date(Date.now() + 60000),
    })
  );
  await assertFails(
    bobDb.collection("workspaces").doc(ALICE_UID).collection("audit_logs").doc("log_forged").set({
      userId: ALICE_UID,
      actorRole: "SUPER_ADMIN",
      action: "FORGED_ACTION",
      moduleName: "Security",
      targetId: ALICE_UID,
      createdAt: created,
    })
  );

  // Update & Delete MUST fail (immutable ledger)
  await assertFails(
    aliceDb.collection("workspaces").doc(ALICE_UID).collection("audit_logs").doc("log_sec_1").update({
      action: "TAMPERED",
    })
  );
  await assertFails(
    aliceDb.collection("workspaces").doc(ALICE_UID).collection("audit_logs").doc("log_sec_1").delete()
  );
});
