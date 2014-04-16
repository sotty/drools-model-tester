import net.vojir.droolsserver.drools.ModelTesterMain;
import org.junit.Test;
import org.kie.api.KieServices;
import org.kie.api.io.Resource;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.net.URL;

public class TestMiner {

    @Test
    public void testMiner() {
        ModelTesterMain.run(
                readResource( "iris.xml" ),
                readResource( "iris1.csv" ),
                "confidence",
                "text" );
    }

    private String readResource( String s ) {
        Resource res = KieServices.Factory.get().getResources().newClassPathResource( s );
        try {
            InputStream stream = res.getInputStream();
            byte[] data = new byte[ stream.available() ];
            stream.read( data );
            stream.close();
            return new String( data );
        } catch ( Exception e ) {
            e.printStackTrace();
        }
        return null;
    }
}
