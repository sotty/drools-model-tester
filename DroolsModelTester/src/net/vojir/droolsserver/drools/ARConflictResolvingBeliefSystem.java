package net.vojir.droolsserver.drools;

import org.drools.core.beliefsystem.BeliefSet;
import org.drools.core.beliefsystem.BeliefSystem;
import org.drools.core.common.InternalFactHandle;
import org.drools.core.common.NamedEntryPoint;
import org.drools.core.common.TruthMaintenanceSystem;

public class ARConflictResolvingBeliefSystem extends ConflictResolvingBeliefSystem<DrlAR,DrlAR> {

    public ARConflictResolvingBeliefSystem( NamedEntryPoint ep, TruthMaintenanceSystem tms ) {
        super( ep, tms );
    }

    @Override
    public BeliefSet newBeliefSet( InternalFactHandle fh ) {
        return new ARConflictResolvingBeliefSet( this, fh );
    }

    public static class ARConflictResolvingBeliefSet extends ConflictResolvingBeliefSet<DrlAR,DrlAR> {

        private ARConflictResolvingBeliefSet( BeliefSystem beliefSystem, InternalFactHandle fh ) {
            super( beliefSystem, fh );
        }

        @Override
        protected void resolveConflicts( DrlAR newJustified, DrlAR newContext ) {
            if ( size() == 1 ) {
                setBestContext( newContext );
                setPrime( newJustified );
            }

            if ( newContext.getConfidenceValue() > getBestContext().getConfidenceValue()
                 || ( newContext.getConfidenceValue() == getBestContext().getConfidenceValue()
                      && newContext.getSupportValue() > getBestContext().getSupportValue() ) ) {
                System.out.println( "Override :" + newContext.getId() + " vs " + getBestContext().getId() );

                setBestContext( newContext );
                setPrime( newJustified );
            }
        }
    }
}
