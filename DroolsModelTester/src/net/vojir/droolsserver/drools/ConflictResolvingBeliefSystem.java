package net.vojir.droolsserver.drools;

import org.drools.core.beliefsystem.BeliefSet;
import org.drools.core.beliefsystem.BeliefSystem;
import org.drools.core.beliefsystem.simple.SimpleBeliefSet;
import org.drools.core.beliefsystem.simple.SimpleBeliefSystem;
import org.drools.core.beliefsystem.simple.SimpleLogicalDependency;
import org.drools.core.common.InternalFactHandle;
import org.drools.core.common.LogicalDependency;
import org.drools.core.common.NamedEntryPoint;
import org.drools.core.common.TruthMaintenanceSystem;
import org.drools.core.reteoo.ObjectTypeConf;
import org.drools.core.spi.PropagationContext;
import org.drools.core.util.LinkedListEntry;
import org.drools.core.util.LinkedListNode;

public abstract class ConflictResolvingBeliefSystem<T,V> extends SimpleBeliefSystem {

    // need to keep a reference here since it's private and inaccesible in the superclass
    private NamedEntryPoint ep;

    public ConflictResolvingBeliefSystem( NamedEntryPoint ep, TruthMaintenanceSystem tms ) {
        super( ep, tms );
        this.ep = ep;
    }

    @Override
    public void insert( LogicalDependency node, BeliefSet beliefSet, PropagationContext context, ObjectTypeConf typeConf ) {
        boolean empty = beliefSet.isEmpty();

        Object current = beliefSet.getFactHandle().getObject();
        beliefSet.add( node.getJustifierEntry() );

        if ( empty ) {
            InternalFactHandle handle = beliefSet.getFactHandle();

            ep.insert( handle,
                       handle.getObject(),
                       node.getJustifier().getRule(),
                       node.getJustifier(),
                       typeConf,
                       null );
        } else {
            ConflictResolvingBeliefSet<T,V> conflictResolvingBeliefSet = ((ConflictResolvingBeliefSet) beliefSet);
            Object prime = conflictResolvingBeliefSet.getPrime();
            if ( current != prime ) {
                System.out.println( "---------------------DETECTED PRIME CHANGE " );
                InternalFactHandle handle = beliefSet.getFactHandle();
                ep.update( handle,
                           true,
                           prime,
                           Long.MAX_VALUE,
                           prime.getClass(),
                           null );
            }

        }
    }


    public static abstract class ConflictResolvingBeliefSet<T,V> extends SimpleBeliefSet {

        private T prime;
        private V bestContext;

        protected ConflictResolvingBeliefSet( BeliefSystem beliefSystem, InternalFactHandle fh ) {
            super( beliefSystem, fh );
        }

        @Override
        public void add( LinkedListNode node ) {
            super.add( node );

            SimpleLogicalDependency ld = (SimpleLogicalDependency) ((LinkedListEntry) node).getObject();
            T justified = (T) ld.getObject();
            V context = (V) ld.getValue();

            resolveConflicts( justified, context );
        }

        protected abstract void resolveConflicts( T justified, V context );

        public V getBestContext() {
            return bestContext;
        }

        public void setBestContext( V bestContext ) {
            this.bestContext = bestContext;
        }

        public T getPrime() {
            return prime;
        }

        public void setPrime( T prime ) {
            this.prime = prime;
        }
    }
}
