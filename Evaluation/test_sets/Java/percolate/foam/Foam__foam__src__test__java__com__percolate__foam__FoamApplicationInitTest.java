package com.percolate.foam;

import android.app.Application;

import org.junit.Test;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Matchers.isNull;
import static org.mockito.Matchers.startsWith;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

/**
 * Foam unit tests for FoamApplicationInit.java
 */
public class FoamApplicationInitTest {

    @Test
    public void testInit() throws Exception {
        final FoamApiKeys mockfoamApiKeys = mock(FoamApiKeys.class);

        Application mockApplication = mock(Application.class);
        FoamApplicationInit foamApplicationInit = new FoamApplicationInit(mockApplication){
            @Override
            FoamApiKeys getFoamApiKeys() {
                return mockfoamApiKeys;
            }
        };

        FoamMain mockFoamMain = mock(FoamMain.class);
        foamApplicationInit.foamMain = mockFoamMain;
        foamApplicationInit.init();

        verify(mockFoamMain).init(eq(mockfoamApiKeys));
        verify(mockFoamMain).start();
    }

    @Test
    public void testInitNoAnnotation() throws Exception {
        Application mockApplication = mock(Application.class);
        Utils mockUtils = UnitTestUtils.mockUtils();

        FoamApplicationInit foamApplicationInit = new FoamApplicationInit(mockApplication);

        FoamMain mockFoamMain = mock(FoamMain.class);
        foamApplicationInit.foamMain = mockFoamMain;
        foamApplicationInit.utils = mockUtils;
        foamApplicationInit.init();

        verify(mockFoamMain, never()).init(any(FoamApiKeys.class));
        verify(mockFoamMain, never()).start();
        verify(mockUtils).logIssue(startsWith("Please add @FoamApiKeys to "), isNull(Throwable.class));
    }

    @Test
    public void testGetFoamApiKeysNull(){
        class LocalMockApplication extends Application{}
        LocalMockApplication application = new LocalMockApplication();

        FoamApplicationInit foamApplicationInit = new FoamApplicationInit(application);
        assertNull(foamApplicationInit.getFoamApiKeys());
    }

    @Test
    public void testGetFoamApiKeys(){
        @FoamApiKeys
        class LocalMockApplication extends Application{}
        LocalMockApplication application = new LocalMockApplication();

        FoamApplicationInit foamApplicationInit = new FoamApplicationInit(application);
        assertNotNull(foamApplicationInit.getFoamApiKeys());
    }

}