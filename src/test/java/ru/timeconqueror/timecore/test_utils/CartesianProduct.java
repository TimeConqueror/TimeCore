package ru.timeconqueror.timecore.test_utils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class CartesianProduct {
    public static <A, B, C, D> List<D> ofThree(Collection<A> listA, Collection<B> listB, Collection<C> listC, ObjectFrom3Factory<A, B, C, D> factory) {
        List<D> resultObjects = new ArrayList<>();

        for (A a : listA) {
            for (B b : listB) {
                for (C c : listC) {
                    D resultObject = factory.create(a, b, c);
                    resultObjects.add(resultObject);
                }
            }
        }
        return resultObjects;
    }

    public interface ObjectFrom3Factory<A, B, C, D> {
        D create(A a, B b, C c);
    }
}
