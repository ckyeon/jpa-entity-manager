package persistence.sql.dml;

import persistence.sql.model.Table;

public class DMLQueryBuilder {

    private final Table table;

    public DMLQueryBuilder(Table table) {
        this.table = table;
    }

    public String buildInsertQuery(Object entity) {
        InsertQueryBuilder insertQueryBuilder = new InsertQueryBuilder(table, entity);
        return insertQueryBuilder.build();
    }

    public String buildFindAllQuery() {
        FindQueryBuilder findQueryBuilder = new FindQueryBuilder(table);
        return findQueryBuilder.build();
    }

    public String buildFindByIdQuery(Object id) {
        FindQueryBuilder findQueryBuilder = new FindQueryBuilder(table);
        return findQueryBuilder.buildById(id);
    }

    public String buildUpdateByIdQuery(Object entity, Object id) {
        UpdateQueryBuilder updateQueryBuilder = new UpdateQueryBuilder(table, entity);
        return updateQueryBuilder.buildById(id);
    }

    public String buildDeleteAllQuery() {
        DeleteQueryBuilder deleteQueryBuilder = new DeleteQueryBuilder(table);
        return deleteQueryBuilder.build();
    }

    public String buildDeleteByIdQuery(Object id) {
        DeleteQueryBuilder deleteQueryBuilder = new DeleteQueryBuilder(table);
        return deleteQueryBuilder.buildById(id);
    }
}
