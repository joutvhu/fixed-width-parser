package com.example;

import com.joutvhu.fixedwidth.parser.FixedParser;

public class Main {
    public static void main(String[] args) {
        String fixedData = "00001joutvhu   giao@example.com    ";
        
        // Try to parse the data (Class first, then data)
        User user = FixedParser.parser().parse(User.class, fixedData);
        
        System.out.println("Parsed User:");
        System.out.println("ID: " + user.getId());
        System.out.println("Username: " + user.getUsername());
        System.out.println("Email: " + user.getEmail());

        // Check if the generated accessor class exists
        try {
            Class<?> accessorClass = Class.forName("com.example.User$FixedAccessor");
            System.out.println("\nSUCCESS: Found generated accessor: " + accessorClass.getName());
        } catch (ClassNotFoundException e) {
            System.out.println("\nFAILURE: Generated accessor com.example.User$FixedAccessor not found!");
        }
        
        // Export back to string
        String exported = FixedParser.parser().export(user);
        System.out.println("\nExported Data:");
        System.out.println("\"" + exported + "\"");
        
        if (fixedData.equals(exported)) {
            System.out.println("\nData integrity check PASSED.");
        } else {
            System.out.println("\nData integrity check FAILED.");
        }
    }
}
