package hudson.plugins.twitter;

import java.lang.reflect.Field;

/**
 * Class to help with setting properties via reflection.
 * 
 * @author justinedelson
 * 
 */
public class ReflectionHelper {

    /**
     * Find a declared field in a class, including super classes.
     * 
     * @param targetClass
     *            the class to search through
     * @param name
     *            the name of the field
     * @return the found Field
     * @throws NoSuchFieldException
     *             if the Field can't be found
     */
    public static Field findDeclaredField(Class targetClass, String name)
            throws NoSuchFieldException {

        // Keep backing up the inheritance hierarchy.
        do {
            try {
                Field field = targetClass.getDeclaredField(name);
                return field;
            } catch (NoSuchFieldException e) {

            }
            targetClass = targetClass.getSuperclass();
        } while (targetClass != null);
        throw new NoSuchFieldException(name);
    }

    /**
     * Set the field, even if it's private, to a boolean value.
     * 
     * @param object
     *            the object
     * @param name
     *            the field name
     * @param value
     *            the field value
     * @throws Exception
     *             if anything bad happens
     */
    public static void setField(Object object, String name, boolean value) throws Exception {
        Field field = findDeclaredField(object.getClass(), name);
        field.setAccessible(true);
        field.setBoolean(object, value);
    }

    /**
     * Set the field.
     * 
     * @param object
     *            the object
     * @param name
     *            the field name
     * @param value
     *            the field value
     * @throws Exception
     *             if anything bad happens
     */
    public static void setField(Object object, String name, Object value) throws Exception {
        Field field = findDeclaredField(object.getClass(), name);
        field.setAccessible(true);
        field.set(object, value);
    }
}
