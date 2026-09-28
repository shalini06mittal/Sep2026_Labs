import java.lang.annotation.*;
import java.lang.reflect.*;
import java.util.*;

/**
 * MiniJpaDemo
 * ------------------------------------------------------------------
 * A pure core-Java simulation of what Spring Boot + Spring Data JPA
 * actually does under the hood. No frameworks, no libraries — just
 * annotations, reflection, and java.lang.reflect.Proxy.
 *
 * Maps to real concepts like this:
 *
 *   @Entity / @Id (custom, below)   -> javax.persistence.Entity / @Id
 *   MiniEntityManager                -> Hibernate's EntityManager / Session
 *   Repository<T, ID> interface      -> Spring Data's JpaRepository<T, ID>
 *   createRepository(...)            -> Spring's repository proxy factory,
 *                                        the thing that creates a working
 *                                        object for your repository interface
 *                                        WITHOUT you ever writing an impl class.
 * ------------------------------------------------------------------
 */
public class MiniJpaDemo {

    // ------------------------------------------------------------------
    // 1. Custom annotations — stand-ins for javax.persistence.Entity / @Id
    // ------------------------------------------------------------------
    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.TYPE)
    @interface Entity {}

    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.FIELD)
    @interface Id {}

    // ------------------------------------------------------------------
    // 2. A sample "entity" — exactly what you'd annotate in real JPA
    // ------------------------------------------------------------------
    @Entity
    static class User {
        @Id
        private Long id;
        private String name;
        private String email;

        User() {} // JPA/Hibernate always needs a no-arg constructor

        User(Long id, String name, String email) {
            this.id = id;
            this.name = name;
            this.email = email;
        }

        @Override
        public String toString() {
            return "User{id=" + id + ", name=" + name + ", email=" + email + "}";
        }
    }

    // ------------------------------------------------------------------
    // 3. MiniEntityManager — a tiny stand-in for what Hibernate really is:
    //    it uses reflection to inspect @Entity/@Id annotations and stores
    //    objects in an in-memory "table" (Class -> (id -> instance)).
    // ------------------------------------------------------------------
    static class MiniEntityManager {
        private final Map<Class<?>, Map<Object, Object>> store = new HashMap<>();

        void persist(Object entity) {
            requireEntity(entity.getClass());
            Object id = readIdValue(entity);
            store.computeIfAbsent(entity.getClass(), k -> new HashMap<>()).put(id, entity);
        }

        Object findById(Class<?> entityClass, Object id) {
            requireEntity(entityClass);
            Map<Object, Object> table = store.get(entityClass);
            return table == null ? null : table.get(id);
        }

        List<Object> findAll(Class<?> entityClass) {
            requireEntity(entityClass);
            Map<Object, Object> table = store.get(entityClass);
            return table == null ? Collections.emptyList() : new ArrayList<>(table.values());
        }

        private void requireEntity(Class<?> clazz) {
            if (!clazz.isAnnotationPresent(Entity.class)) {
                throw new IllegalArgumentException(clazz.getSimpleName() + " is not annotated with @Entity");
            }
        }

        private Object readIdValue(Object entity) {
            for (Field field : entity.getClass().getDeclaredFields()) {
                if (field.isAnnotationPresent(Id.class)) {
                    field.setAccessible(true); // reflection bypassing private access, same as Hibernate does
                    try {
                        return field.get(entity);
                    } catch (IllegalAccessException e) {
                        throw new RuntimeException(e);
                    }
                }
            }
            throw new IllegalStateException("No @Id field found on " + entity.getClass());
        }
    }

    // ------------------------------------------------------------------
    // 4. A repository interface — exactly the shape of Spring Data's
    //    JpaRepository<T, ID>. Notice there is NO class anywhere in this
    //    file that implements this interface by hand.
    // ------------------------------------------------------------------
    interface Repository<T, ID> {
        void save(T entity);
        T findById(ID id);
        List<T> findAll();
    }

    // ------------------------------------------------------------------
    // 5. The proxy factory — this IS what Spring Data JPA does at
    //    application startup for every interface you mark @Repository:
    //    it builds a real, working object implementing your interface
    //    on the fly, using java.lang.reflect.Proxy, and routes every
    //    method call into generic logic (here: our MiniEntityManager).
    // ------------------------------------------------------------------
    @SuppressWarnings("unchecked")
    static <T, ID> Repository<T, ID> createRepository(Class<T> entityClass, MiniEntityManager em) {
        InvocationHandler handler = (proxy, method, args) -> {
            switch (method.getName()) {
                case "save":
                    em.persist(args[0]);
                    return null;
                case "findById":
                    return em.findById(entityClass, args[0]);
                case "findAll":
                    return em.findAll(entityClass);
                case "toString":
                    return "DynamicProxyRepository<" + entityClass.getSimpleName() + ">";
                default:
                    throw new UnsupportedOperationException(method.getName());
            }
        };

        return (Repository<T, ID>) Proxy.newProxyInstance(
                MiniJpaDemo.class.getClassLoader(),
                new Class<?>[]{Repository.class},
                handler
        );
    }

    // ------------------------------------------------------------------
    // 6. Demo — this is what your Spring Boot code effectively looks
    //    like; everything above is what's happening behind that one line.
    // ------------------------------------------------------------------
    public static void main(String[] args) {
        MiniEntityManager entityManager = new MiniEntityManager();

        // In Spring, this line is replaced by simply @Autowire-ing
        // UserRepository — Spring builds this same kind of proxy for you.
        Repository<User, Long> userRepository = createRepository(User.class, entityManager);

        userRepository.save(new User(1L, "Alice", "alice@example.com"));
        userRepository.save(new User(2L, "Bob", "bob@example.com"));

        System.out.println("Repository instance -> " + userRepository);
        System.out.println("findById(1)          -> " + userRepository.findById(1L));
        System.out.println("findAll()            -> " + userRepository.findAll());
    }
}
