package ru.prestu.authident.domain.model.types;

import org.hibernate.HibernateException;
import org.hibernate.engine.spi.SharedSessionContractImplementor;
import org.hibernate.usertype.UserType;
import java.io.Serializable;
import java.sql.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class DoubleArrayType implements UserType {

    private final int[] arrayTypes = new int[] { Types.ARRAY };

    public int[] sqlTypes() {
        return arrayTypes;
    }

    public Class<List> returnedClass() {
        return List.class;
    }

    public boolean equals(Object x, Object y) throws HibernateException {
        return x == null ? y == null : x.equals(y);
    }

    public int hashCode(Object x) throws HibernateException {
        return x == null ? 0 : x.hashCode();
    }

    @Override
    public Object nullSafeGet(ResultSet rs, String[] names, SharedSessionContractImplementor sharedSessionContractImplementor, Object o) throws HibernateException, SQLException {
        if (names != null && names.length > 0 && rs != null && rs.getArray(names[0]) != null) {
            Object array = rs.getArray(names[0]).getArray();
            if (array instanceof Double[])
                return Arrays.asList((Double[]) array);
            else
                return Arrays.asList(convertNumberArrayToDouble((Number[]) array));
        }

        return null;
    }

    @Override
    @SuppressWarnings("unchecked")
    public void nullSafeSet(PreparedStatement preparedStatement, Object value, int index, SharedSessionContractImplementor sharedSessionContractImplementor) throws HibernateException, SQLException {
        if (value != null) {
            List<Double> list = (List<Double>) value;
            Double[] castObject = list.toArray(new Double[list.size()]);
            Array array = sharedSessionContractImplementor.connection().createArrayOf("float8", castObject);
            preparedStatement.setArray(index, array);
        } else {
            preparedStatement.setNull(index, arrayTypes[0]);
        }
    }

    private Double[] convertNumberArrayToDouble(Number[] array) {
        Double[] doubleArray = new Double[array.length];
        for (int i = 0; i < array.length; i++)
            doubleArray[i] = array[i].doubleValue();
        return doubleArray;
    }

    @SuppressWarnings("unchecked")
    public Object deepCopy(Object value) throws HibernateException {
        if (value == null)
            return null;

        List<Double> list = (List<Double>) value;
        ArrayList<Double> clone = new ArrayList<>();
        for (Object doubleOn : list)
            clone.add((Double) doubleOn);

        return clone;
    }

    public boolean isMutable() {
        return false;
    }

    public Serializable disassemble(Object value) throws HibernateException {
        return (Serializable) value;
    }

    public Object assemble(Serializable cached, Object owner) throws HibernateException {
        return cached;
    }

    public Object replace(Object original, Object target, Object owner) throws HibernateException {
        return original;
    }
}
//
//@Entity
//@Table(name = "posts", schema = "public")
//
//public class Posts extends IEntity {
//    @Column(unique=true, name = "title") private String title;
//    @Column(name = "accountid")
//    @Type(type = "com.varam.blog.hql.StringArrayType")
//    private String[] tags;
//
////Setters and getter methods
//}