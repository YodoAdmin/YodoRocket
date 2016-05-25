package co.yodo.launcher;

import android.test.ActivityInstrumentationTestCase2;

import java.util.concurrent.Semaphore;

import co.yodo.launcher.network.model.ServerResponse;
import co.yodo.launcher.helper.AppUtils;
import co.yodo.launcher.ui.RegistrationActivity;
import co.yodo.launcher.network.YodoRequest;

/**
 * <a href="http://d.android.com/tools/testing/testing_android.html">Testing Fundamentals</a>
 */
public class RegistrationActivityTest extends ActivityInstrumentationTestCase2<RegistrationActivity> implements YodoRequest.RESTListener {
    /** The activity object */
    private RegistrationActivity activity;

    /** Authentication Number */
    private String hardwareToken;

    /** Server Response */
    private ServerResponse response;

    /** Semaphore */
    private Semaphore semaphore;

    public RegistrationActivityTest() {
        super( RegistrationActivity.class );
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
     * Test different registration requests (hardware - registration code)
     * @throws Exception
     */
    public void testRegistration() throws Exception {
        // Wrong Registration Code
        YodoRequest.getInstance().requestRegistration( activity, hardwareToken, "01" );
        semaphore.acquire();

        assertNotNull( response );
        assertEquals( ServerResponse.ERROR_DUP_AUTH, response.getCode() );
        response = null;

        // No Registration Code
        YodoRequest.getInstance().requestRegistration( activity, hardwareToken, "" );
        semaphore.acquire();

        assertNotNull( response );
        assertEquals( ServerResponse.ERROR_FAILED, response.getCode() );
        response = null;

        // No Hardware Token
        YodoRequest.getInstance().requestRegistration( activity, "", "" );
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