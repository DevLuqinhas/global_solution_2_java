package br.com.fiap.gs.app;

import br.com.fiap.gs.repository.ConnectionFactory;

public class TestOracleDB {
    public static void main(String[] args) {
        try {
            ConnectionFactory factory = new ConnectionFactory();
            factory.getConnection();
            System.out.println("✅ Conexão com Oracle OK!");
        } catch (Exception e) {
            System.out.println("❌ Erro ao conectar: " + e.getMessage());
        }
    }
}

