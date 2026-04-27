package com.example;

import com.joutvhu.fixedwidth.parser.FixedParser;

public class Main {
    public static void main(String[] args) {
        testUser();
        testAdmin();
        testPolymorphism();
    }

    private static void testUser() {
        System.out.println("=== Testing User with Nested Object, Collection, and Map ===");
        // U(1) + 00001(5) + joutvhu   (10) + Saigon    70000(15) + 02(2) + USER ADMIN(10) + KEY01VAL01KEY02VAL02(20)
        // Total: 63 chars
        String userData = "U00001joutvhu   Saigon    7000002USER ADMINKEY01VAL01KEY02VAL02";
        User user = FixedParser.parser().parse(User.class, userData);
        
        System.out.println("Parsed: " + user);
        String exported = FixedParser.parser().export(user);
        System.out.println("Exported: \"" + exported + "\"");
        System.out.println("Integrity: " + userData.equals(exported));
        System.out.println();
    }

    private static void testAdmin() {
        System.out.println("=== Testing Admin Subclass ===");
        // A (1) + 00002 (5) + root      (10) + SUPERUSER (10)
        // Total: 1 + 5 + 10 + 10 = 26 chars
        String adminData = "A00002root      SUPERUSER ";
        Admin admin = FixedParser.parser().parse(Admin.class, adminData);
        
        System.out.println("Parsed: " + admin);
        String exported = FixedParser.parser().export(admin);
        System.out.println("Exported: \"" + exported + "\"");
        System.out.println("Integrity: " + adminData.equals(exported));
        System.out.println();
    }

    private static void testPolymorphism() {
        System.out.println("=== Testing Polymorphic Parsing (AbstractUser) ===");
        String userData = "U00001joutvhu   Saigon    7000002USER ADMIN";
        String adminData = "A00002root      SUPERUSER ";

        AbstractUser u = FixedParser.parser().parse(AbstractUser.class, userData);
        AbstractUser a = FixedParser.parser().parse(AbstractUser.class, adminData);

        System.out.println("Parsed U as: " + u.getClass().getSimpleName());
        System.out.println("Parsed A as: " + a.getClass().getSimpleName());
        
        if (u instanceof User && a instanceof Admin) {
            System.out.println("Polymorphic detection: SUCCESS");
        } else {
            System.out.println("Polymorphic detection: FAILURE");
        }
    }
}
