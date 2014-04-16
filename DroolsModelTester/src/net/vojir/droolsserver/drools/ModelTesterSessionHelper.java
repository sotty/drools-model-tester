package net.vojir.droolsserver.drools;


import org.drools.core.BeliefSystemType;
import org.drools.core.RuleBaseConfiguration;
import org.drools.core.beliefsystem.BeliefSystem;
import org.drools.core.common.BeliefSystemFactory;
import org.drools.core.common.InternalRuleBase;
import org.drools.core.common.NamedEntryPoint;
import org.drools.core.common.TruthMaintenanceSystem;
import org.drools.core.impl.KnowledgeBaseImpl;
import org.drools.core.reteoo.KieComponentFactory;
import org.kie.api.KieBase;
import org.kie.api.KieBaseConfiguration;
import org.kie.api.KieServices;
import org.kie.api.builder.KieBuilder;
import org.kie.api.builder.KieFileSystem;
import org.kie.api.builder.KieModule;
import org.kie.api.builder.Message;
import org.kie.api.conf.DeclarativeAgendaOption;
import org.kie.api.conf.EqualityBehaviorOption;
import org.kie.api.event.rule.DebugAgendaEventListener;
import org.kie.api.runtime.KieContainer;
import org.kie.api.runtime.KieSession;
import org.kie.api.runtime.StatelessKieSession;
import org.kie.api.runtime.rule.Match;
import org.kie.internal.io.ResourceFactory;

@SuppressWarnings("restriction")
public class ModelTesterSessionHelper {

    private static enum BetterARMethod {
        CONFIDENCE("confidence"),
        SUPPORT("support"),
        CONF_SUPP("csCombination"),
        MORESPECIFIC("longerAntecedent"),
        LESSSPECIFIC("shorterAntecedent");

        private String name;

        BetterARMethod( String n ) {
            this.name = n;
        }

        public static BetterARMethod parse( String s ) {
            for ( BetterARMethod method : BetterARMethod.values() ) {
                if ( method.name.equals( s ) ) {
                    return method;
                }
            }
            return null;
        }
    }

    private static BetterARMethod betterARMethod;
	
	/**
	 * Funkce pro vytvo�en� nov�ho DLR �et�zce - inicializace pomoc� v�choz�ch import�
	 */
	public static String prepareDrlString(String drl){
		StringBuffer drlString=new StringBuffer();
		drlString.append("import net.vojir.droolsserver.drools.DrlObj;");
		drlString.append("import net.vojir.droolsserver.drools.DrlAR;");
		drlString.append("import " + Match.class.getName() + ";" );
		drlString.append("import function net.vojir.droolsserver.drools.ModelTesterSessionHelper.isBetterAR;");

        drlString.append("\n\n rule zeroRule salience -1000 \n" +
                         "when $ar:DrlAR(id!=\"\") \n" +
                         "then " +
                         "  System.out.println( 'ZERO' ); " +
                         "  $ar.setId(\"\"); " +
                         "  update($ar) \n" +
                         "end \n" +
                         "" +
                         "query result( DrlAR $ar ) " +
                         "  $ar := DrlAR() " +
                         "end " +
                         "" +
                         "" +

/*

                         "rule 'Block by confidence' @Direct " +
                         "dialect 'mvel' " +
                         "when " +
                         "  $m1 : Match( associationRole == 'premise'  ) " +
                         "  $m2 : Match( this != $m1, associationRole == 'premise', " +
                         "               confidence > $m1.confidence ||" +
                         "               confidence == $m1.confidence && support > $m1.support ) " +
                         "then " +
                         "  kcontext.cancelMatch( $m1 ); " +
                         "end " +
*/


                         "rule Log " +
                         "salience 1000 " +
                         "when $ar : DrlAR() " +
                         "then System.out.println( '   >>>> ' + $ar ) ; " +
                         "end " +
                         "" );

		drlString.append(drl);

		return drlString.toString();
	}

	/**
	 * Statick� funkce pro vytvo�en� stateless session
	 * @param drlString
	 * @return
	 */
    public static KieBase prepareKieBase(String drlString) {

        KieServices ks = KieServices.Factory.get();
        KieFileSystem kfs = ks.newKieFileSystem();
        kfs.write( ResourceFactory.newByteArrayResource( drlString.getBytes() ).setTargetPath("rules.drl") );

        KieBuilder kbuilder = KieServices.Factory.get().newKieBuilder(kfs);
        kbuilder.buildAll();
        if ( kbuilder.getResults().hasMessages( Message.Level.ERROR ) ) {
            System.out.println( kbuilder.getResults().getMessages( Message.Level.ERROR ));
        }

        ks.newKieContainer(kbuilder.getKieModule().getReleaseId()).getKieBase();

        KieBaseConfiguration kconf = ks.newKieBaseConfiguration();
        kconf.setOption( DeclarativeAgendaOption.ENABLED );
        kconf.setOption( EqualityBehaviorOption.EQUALITY );
        (( RuleBaseConfiguration ) kconf).setComponentFactory( new KieComponentFactory() {
            @Override
            public BeliefSystemFactory getBeliefSystemFactory() {
                return new BeliefSystemFactory() {
                    @Override
                    public BeliefSystem createBeliefSystem( BeliefSystemType type, NamedEntryPoint ep, TruthMaintenanceSystem tms ) {
                        return new ARConflictResolvingBeliefSystem( ep, tms );
                    }
                };
            }
        });

        KieContainer kContainer = ks.newKieContainer( kbuilder.getKieModule().getReleaseId() );
        KieBase kieBase = kContainer.newKieBase( kconf );

        return kieBase;
	}
    

    //-----------------------------------------------------------------------------------
	public static BetterARMethod getBetterARMethod() {
		return betterARMethod;
	}

	public static void setBetterARMethod(String betterARMethod) {
		ModelTesterSessionHelper.betterARMethod = BetterARMethod.parse( betterARMethod );

        if ( ModelTesterSessionHelper.betterARMethod == null ) {
			System.out.println( "NEEXISTUJE metoda "+betterARMethod );
			System.exit(0);
		}
	}
    

}
