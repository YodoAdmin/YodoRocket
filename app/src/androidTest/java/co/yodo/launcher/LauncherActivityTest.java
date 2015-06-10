package co.yodo.launcher;

import android.test.ActivityInstrumentationTestCase2;

import java.util.concurrent.Semaphore;

import co.yodo.launcher.data.ServerResponse;
import co.yodo.launcher.helper.AppUtils;
import co.yodo.launcher.main.LauncherActivity;
import co.yodo.launcher.main.RegistrationActivity;
import co.yodo.launcher.net.YodoRequest;

/**
 * <a href="http://d.android.com/tools/testing/testing_android.html">Testing Fundamentals</a>
 */
public class LauncherActivityTest extends ActivityInstrumentationTestCase2<LauncherActivity> implements YodoRequest.RESTListener {
    /** The activity object */
    private LauncherActivity activity;

    /** Authentication Number */
    private String hardwareToken;

    /** Merchant PIP */
    private String merchPIP = "test";

    /** Server Response */
    private ServerResponse response;

    /** Semaphore */
    private Semaphore semaphore;

    public LauncherActivityTest() {
        super( LauncherActivity.class );
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        activity      = getActivity();
        hardwareToken = AppUtils.getHardwareToken( activity );
        semaphore     = new Semaphore( 0 );

        YodoRequest.getInstance().setListener( this );
    }

    public void test() throws Exception {
        assertNotNull( hardwareToken );
        assertNotNull( YodoRequest.getInstance() );
    }

    /**
     * Test different history requests (hardware - merchPIP)
     * @throws Exception
     */
    public void testHistory() throws Exception {
        // All Correct
        YodoRequest.getInstance().requestHistory( activity, hardwareToken, merchPIP );
        semaphore.acquire();

        assertNotNull( response );
        assertEquals( ServerResponse.AUTHORIZED, response.getCode() );
        response = null;

        // No MerchPIP
        YodoRequest.getInstance().requestHistory( activity, hardwareToken, "" );
        semaphore.acquire();

        assertNotNull( response );
        assertEquals( ServerResponse.ERROR_FAILED, response.getCode() );
        response = null;

        // No Hardware Token
        YodoRequest.getInstance().requestHistory( activity, "", merchPIP );
        semaphore.acquire();

        assertNotNull( response );
        assertEquals( ServerResponse.ERROR_FAILED, response.getCode() );
        response = null;
    }

    /**
     * Test different daily history requests (hardware - merchPIP)
     * @throws Exception
     */
    public void testDailyHistory() throws Exception {
        // All Correct
        YodoRequest.getInstance().requestDailyHistory( activity, hardwareToken, merchPIP );
        semaphore.acquire();

        assertNotNull( response );
        assertEquals( ServerResponse.AUTHORIZED, response.getCode() );
        response = null;

        // No MerchPIP
        YodoRequest.getInstance().requestDailyHistory( activity, hardwareToken, "" );
        semaphore.acquire();

        assertNotNull( response );
        assertEquals( ServerResponse.ERROR_FAILED, response.getCode() );
        response = null;

        // No Hardware Token
        YodoRequest.getInstance().requestDailyHistory( activity, "", merchPIP );
        semaphore.acquire();

        assertNotNull( response );
        assertEquals( ServerResponse.ERROR_FAILED, response.getCode() );
        response = null;
    }

    /**
     * Test different logo requests (hardware)
     * @throws Exception
     */
    public void testLogo() throws Exception {
        // All Correct
        YodoRequest.getInstance().requestLogo( activity, hardwareToken );
        semaphore.acquire();

        assertNotNull( response );
        assertEquals( ServerResponse.AUTHORIZED, response.getCode() );
        response = null;

        // No Hardware Token
        YodoRequest.getInstance().requestLogo( activity, "" );
        semaphore.acquire();

        assertNotNull( response );
        assertEquals( ServerResponse.ERROR_FAILED, response.getCode() );
        response = null;
    }

    /**
     * Test different logo requests (hardware)
     * @throws Exception
     */
    public void testEchange() throws Exception {
        // Wrong Client
        YodoRequest.getInstance().requestExchange( activity,
                hardwareToken, "", "0.00", "0.00", "0.00" , 0.00, 0.00, "CAD" );
        semaphore.acquire();

        assertNotNull( response );
        assertEquals( ServerResponse.ERROR_FAILED, response.getCode() );
        response = null;
    }

    /**
     * Test different logo requests (hardware)
     * @throws Exception
     */
    public void testAlternate() throws Exception {
        // Wrong Client
        YodoRequest.getInstance().requestAlternate( activity,
                hardwareToken, "", "0.00", "0.00", "0.00" , 0.00, 0.00, "CAD" );
        semaphore.acquire();

        assertNotNull( response );
        assertEquals( ServerResponse.ERROR_FAILED, response.getCode() );
        response = null;
    }

    @Override
    public void onResponse(YodoRequest.RequestType type, ServerResponse response) {
        this.response = response;
        semaphore.release();
    }
}