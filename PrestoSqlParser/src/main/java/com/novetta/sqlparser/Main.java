package com.novetta.sqlparser;

import com.facebook.presto.sql.SqlFormatter;
import com.facebook.presto.sql.parser.SqlParser;
import com.facebook.presto.sql.parser.StatementSplitter;
import com.facebook.presto.sql.tree.AstVisitor;
import com.facebook.presto.sql.tree.ColumnDefinition;
import com.facebook.presto.sql.tree.CreateTable;
import com.facebook.presto.sql.tree.Node;
import com.facebook.presto.sql.tree.Statement;
import com.google.common.base.Charsets;
import com.google.common.io.Files;
import java.io.File;
import java.util.Optional;

public class Main {

    public static void main(final String[] args) throws Exception {
        final String ddl = Files.toString(new File("src/main/sql/ddl.sql"), Charsets.UTF_8);

        final SqlParser parser = new SqlParser();
        final StatementSplitter splitter = new StatementSplitter(ddl);
        for (final StatementSplitter.Statement statement : splitter.getCompleteStatements()) {
            final Statement parsedStatement = parser.createStatement(statement.statement());

            System.out.println(parsedStatement);

            final String formattedStatement = parsedStatement.accept(new AstVisitor<String, Void>() {

                @Override
                protected String visitNode(final Node node, final Void c) {
                    return SqlFormatter.formatSql(node, Optional.empty());
                }

            }, null);

            System.out.println(formattedStatement);

            parsedStatement.accept(new AstVisitor<Void, Void>() {

                @Override
                protected Void visitCreateTable(final CreateTable ct, final Void c) {
                    System.out.println(ct.getName());

                    ct.getElements().forEach(te -> {
                        te.accept(new AstVisitor<Void, Void>() {

                            @Override
                            protected Void visitColumnDefinition(final ColumnDefinition cd, final Void c) {
                                System.out.println("  " + cd.getName() + " : " + cd.getType());
                                return null;
                            }

                        }, null);
                    });
                    return null;
                }

            }, null);

            System.out.println();
        }
    }
}
