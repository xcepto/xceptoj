package org.xcepto.xceptoj.docs.adapters;

import org.xcepto.xceptoj.TransitionBuilder;
import org.xcepto.xceptoj.XceptoAdapter;
import org.xcepto.xceptoj.exceptions.XceptoAdapterInitializationException;
import org.xcepto.xceptoj.exceptions.XceptoAdapterTerminationException;
import org.xcepto.xceptoj.interfaces.ServiceCollection;
import org.xcepto.xceptoj.interfaces.ServiceProvider;

public class OrderAdapterExample {

    public enum OrderStatus {
        FULFILLED,
        PENDING
    }

    public static final class OrderAdapter extends XceptoAdapter {
        private TransitionBuilder builder;

        @Override
        protected void injectBuilder(TransitionBuilder b) {
            super.injectBuilder(b);
            this.builder = b;
        }

        public OrderFlowBuilder order(String orderId) {
            return new OrderFlowBuilder(builder).withOrderId(orderId);
        }

        @Override
        protected void initialize(ServiceProvider serviceProvider) throws XceptoAdapterInitializationException {
        }

        @Override
        protected void addServices(ServiceCollection serviceCollection) {
        }

        @Override
        protected void terminate() throws XceptoAdapterTerminationException {
        }
    }

    public static final class OrderAdapterBuilder {
        private final TransitionBuilder transitionBuilder;

        public OrderAdapterBuilder(TransitionBuilder builder) {
            this.transitionBuilder = builder;
        }

        public OrderAdapter build() {
            var adapter = new OrderAdapter();
            transitionBuilder.registerAdapter(adapter);
            return adapter;
        }
    }

    public static final class OrderFlowBuilder {
        private final TransitionBuilder builder;
        private String orderId = "";
        private int amount;

        public OrderFlowBuilder(TransitionBuilder builder) {
            this.builder = builder;
        }

        public OrderFlowBuilder withOrderId(String id) {
            this.orderId = id;
            return this;
        }

        public OrderFlowBuilder withAmount(int amount) {
            this.amount = amount;
            return this;
        }

        public void shouldReachStatus(OrderStatus status) {
            // would add state to the state machine
        }
    }
}
