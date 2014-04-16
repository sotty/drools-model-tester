package net.vojir.droolsserver.drools;

import java.io.PrintWriter;
import java.io.Reader;
import java.io.StringReader;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;

import net.vojir.droolsserver.xml.AssociationRulesXmlParser;


import com.googlecode.jcsv.reader.CSVReader;
import com.googlecode.jcsv.reader.internal.CSVReaderBuilder;
import org.kie.api.KieBase;
import org.kie.api.event.rule.AfterMatchFiredEvent;
import org.kie.api.event.rule.AgendaEventListener;
import org.kie.api.event.rule.AgendaGroupPoppedEvent;
import org.kie.api.event.rule.AgendaGroupPushedEvent;
import org.kie.api.event.rule.BeforeMatchFiredEvent;
import org.kie.api.event.rule.MatchCancelledEvent;
import org.kie.api.event.rule.MatchCreatedEvent;
import org.kie.api.event.rule.RuleFlowGroupActivatedEvent;
import org.kie.api.event.rule.RuleFlowGroupDeactivatedEvent;
import org.kie.api.runtime.KieSession;
import org.kie.api.runtime.StatelessKieSession;
import org.kie.api.runtime.rule.QueryResults;
import org.kie.api.runtime.rule.Variable;
import org.kie.internal.runtime.StatefulKnowledgeSession;

/**
 * T��da slou��c� ke kontrole p�esnosti a �plnosti modelu tvo�en�ho asocia�n�mi pravidly (na��taj�c� data z DB, vyu��vaj�c� znalostn� b�zi v drools)
 * @author Standa
 *
 */
@SuppressWarnings("restriction")
public class ModelTester {
	
	private KieBase droolsKieBase;
	//��ta�e
	private int rowsPositiveMatch;
	private int rowsNegativeMatch;
	private int rowsError;
	private int rowsTotalCount;
    private int rulesFired;
	final static String xslTemlateResourceName="ar2drl.xslt";
	
	public ModelTester( KieBase droolsKieBase){
		this.droolsKieBase=droolsKieBase;
	}
	
	public void testAllRows(String csvContent,String betterRuleSelectionMethod){
		ModelTesterSessionHelper.setBetterARMethod(betterRuleSelectionMethod);
		testAllRows(csvContent);
	}
	
	/**
	 * Funkce pro projit� jednotliv�ch ��dk� a otestov�n�, jestli odpov�daj� asocia�n�m pravidl�m v drools stateless session
	 * @param csvContent
	 */
	public void testAllRows(String csvContent){
		Reader reader = new StringReader(csvContent);
		resetCounters();
		String[] header;
		int columnsCount;
		String[] row;
		Collection<Object> rowsObjects=new LinkedList<Object>();
		
		CSVReader<String[]> csvParser = CSVReaderBuilder.newDefaultReader(reader);
		Iterator<String[]> iterator = csvParser.iterator();
		//p�ipraven� p�ehledu z�hlav�
		header = iterator.next();
		columnsCount=header.length;

        CountingRuleEventListener listener = new CountingRuleEventListener();
        //jednotliv� ��dky
		while(iterator.hasNext()){
            KieSession droolsSession = droolsKieBase.newKieSession();

			//kontrola jednoho ��dku z datov� matice
			row=iterator.next();
			setRowsTotalCount(getRowsTotalCount() + 1);
			if (row.length != columnsCount){
				setRowsError(getRowsError() + 1);
				continue;
			}
			//vytvo�en� kolekce a test pomoc� drools
			for (int i=0;i<columnsCount;i++){
				droolsSession.insert( new DrlObj( header[ i ], row[ i ] ) );
			}

            droolsSession.addEventListener( listener );
            droolsSession.fireAllRules();

            QueryResults results = droolsSession.getQueryResults( "result", Variable.v );
            if ( results.size() != 1 ) {
                throw new IllegalStateException( "Only 1 result expected, we found " + results.size() );
            }
            DrlAR drlAR = (DrlAR) results.iterator().next().get( "$ar" );

            setRulesFired( listener.getFireCount() );
			if (drlAR.isCheckedOk()){
				setRowsPositiveMatch(getRowsPositiveMatch() + 1);
			}else if(!drlAR.getBestId().equals("")){
				setRowsNegativeMatch(getRowsNegativeMatch() + 1);
			}

            droolsSession.dispose();
			//--kontrola jednoho ��dku z datov� matice			
		}
		
	}
	
	/**
	 * Funkce pro vynulov�n� ��ta��
	 */
	private void resetCounters(){
		this.setRowsPositiveMatch(0);
		this.setRowsNegativeMatch(0);
		this.setRowsError(0);
		this.setRowsTotalCount(0);
	}


	/**
	 * @return the rowsPositiveMatch
	 */
	public int getRowsPositiveMatch() {
		return rowsPositiveMatch;
	}


	/**
	 * @param rowsPositiveMatch the rowsPositiveMatch to set
	 */
	public void setRowsPositiveMatch(int rowsPositiveMatch) {
		this.rowsPositiveMatch = rowsPositiveMatch;
	}


	/**
	 * @return the rowsNegativeMatch
	 */
	public int getRowsNegativeMatch() {
		return rowsNegativeMatch;
	}


	/**
	 * @param rowsNegativeMatch the rowsNegativeMatch to set
	 */
	public void setRowsNegativeMatch(int rowsNegativeMatch) {
		this.rowsNegativeMatch = rowsNegativeMatch;
	}


	/**
	 * @return the rowsError
	 */
	public int getRowsError() {
		return rowsError;
	}


	/**
	 * @param rowsError the rowsError to set
	 */
	public void setRowsError(int rowsError) {
		this.rowsError = rowsError;
	}


	/**
	 * @return the rowsTotalCount
	 */
	public int getRowsTotalCount() {
		return rowsTotalCount;
	}


	/**
	 * @param rowsTotalCount the rowsTotalCount to set
	 */
	public void setRowsTotalCount(int rowsTotalCount) {
		this.rowsTotalCount = rowsTotalCount;
	}

    public int getRulesFired() {
        return rulesFired;
    }

    public void setRulesFired( int rulesFired ) {
        this.rulesFired = rulesFired;
    }

    /**
	 * Statick� funkce pro vytvo�en� nov�ho modeltesteru na z�klad� XML �et�zce a transforma�n� �ablony
	 * @param xmlString
	 * @return
	 * @throws Exception
	 */
	public static ModelTester prepareFromXml(String xmlString) throws Exception{
		String drlString;
		try {
			drlString = AssociationRulesXmlParser.transformBigXml(xmlString, xslTemlateResourceName);
		} catch (Exception e) {
			throw new Exception("Transformation from XML to DRL failed!",e);
		}
    	
    	drlString = ModelTesterSessionHelper.prepareDrlString(drlString);
    	
    	PrintWriter writer = new PrintWriter("drl.txt", "UTF-8");
    	writer.println(drlString);
    	writer.close();
    	
    	//System.out.println("DRL string prepared");
    	
    	KieBase kieBase = ModelTesterSessionHelper.prepareKieBase( drlString );

    	//System.out.println("StatelessSession created");
    	
    	return new ModelTester(kieBase);
    }


    private class CountingRuleEventListener implements AgendaEventListener {
        int fireCount = 0;
        public void matchCreated( MatchCreatedEvent event ) { }
        public void matchCancelled( MatchCancelledEvent event ) {}
        public void beforeMatchFired( BeforeMatchFiredEvent event ) {}
        public void afterMatchFired( AfterMatchFiredEvent event ) { fireCount++; }
        public void agendaGroupPopped( AgendaGroupPoppedEvent event ) {}
        public void agendaGroupPushed( AgendaGroupPushedEvent event ) {}
        public void beforeRuleFlowGroupActivated( RuleFlowGroupActivatedEvent event ) {}
        public void afterRuleFlowGroupActivated( RuleFlowGroupActivatedEvent event ) {}
        public void beforeRuleFlowGroupDeactivated( RuleFlowGroupDeactivatedEvent event ) {}
        public void afterRuleFlowGroupDeactivated( RuleFlowGroupDeactivatedEvent event ) {}

        public int getFireCount() {
            return fireCount;
        }
    }
}
