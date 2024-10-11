package kr.rtuserver.lib.common.api.cdi.factories;

import kr.rtuserver.lib.common.api.cdi.beans.BeanHolder;

import java.util.function.Supplier;

/**
 * @author Mihai Alexandru
 * @date 22.08.2018
 */
public interface BeanHolderFactory {

    BeanHolder getBeanHolder(Class<?> clazz, Supplier<?> instanceSupplier);
}
