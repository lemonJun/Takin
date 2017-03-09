package net.cassite.ioc.testioc;

import lemon.ioc.di.annotations.Inject;
import lemon.ioc.di.annotations.ScopeAttr;

public class Bean2 {
    private BeanA beanA;

    public BeanA getBeanA() {
        return beanA;
    }

    public void setBeanA(@ScopeAttr("beanA") BeanA beanA) {
        this.beanA = beanA;
    }
}
