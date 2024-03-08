package persistence.entity;

import jakarta.persistence.EntityExistsException;
import persistence.sql.model.PKColumn;
import persistence.sql.model.Table;

import java.util.Objects;

public class SimpleEntityManger implements EntityManager {

    private final EntityPersister persister;
    private final EntityLoader loader;
    private final PersistenceContext persistenceContext;

    public SimpleEntityManger(EntityPersister persister, EntityLoader loader) {
        this.persister = persister;
        this.loader = loader;
        this.persistenceContext = new SimplePersistenceContext();
    }

    @Override
    public <T> T find(Class<T> clazz, EntityId id) {
        T cachedEntity = persistenceContext.getEntity(clazz, id);
        if (cachedEntity != null) {
            return cachedEntity;
        }

        T findEntity = loader.read(clazz, id);
        persistenceContext.addEntity(id, findEntity);
        return findEntity;
    }

    @Override
    public void persist(Object entity) {
        if (isExist(entity)) {
            throw new EntityExistsException();
        }
        EntityId id = persister.create(entity);
        persistenceContext.addEntity(id, entity);
    }

    @Override
    public void merge(Object entity) {
        if (!isExist(entity)) {
            EntityId id = persister.create(entity);
            persistenceContext.addEntity(id, entity);
            return;
        }

        if (isDirty(entity)) {
            EntityId id = persister.update(entity);
            persistenceContext.addEntity(id, entity);
        }
    }

    private boolean isExist(Object entity) {
        return persistenceContext.isCached(entity) || loader.isExist(entity);
    }

    private boolean isDirty(Object entity) {
        Class<?> clazz = entity.getClass();
        Table table = new Table(clazz);

        EntityId id = getEntityId(entity);
        Object snapshot = persistenceContext.getDatabaseSnapshot(id, entity);
        if (snapshot == null) {
            snapshot = find(clazz, id);
        }

        EntityBinder entityBinder = new EntityBinder(entity);
        EntityBinder snapshotBinder = new EntityBinder(snapshot);

        return table.getColumns()
                .stream()
                .anyMatch(column -> {
                    Object entityValue = entityBinder.getValue(column);
                    Object snapshotValue = snapshotBinder.getValue(column);
                    return !Objects.equals(entityValue, snapshotValue);
                });
    }

    private EntityId getEntityId(Object entity) {
        Class<?> clazz = entity.getClass();
        Table table = new Table(clazz);

        EntityBinder entityBinder = new EntityBinder(entity);

        PKColumn pkColumn = table.getPKColumn();
        Object idValue = entityBinder.getValue(pkColumn);
        return new EntityId(idValue);
    }

    @Override
    public void remove(Object entity) {
        persistenceContext.removeEntity(entity);
        persister.delete(entity);
    }
}
