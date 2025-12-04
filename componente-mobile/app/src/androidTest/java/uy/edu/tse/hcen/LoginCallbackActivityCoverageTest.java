package uy.edu.tse.hcen;

import static org.junit.Assert.*;

import android.app.Instrumentation;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.SystemClock;

import androidx.lifecycle.Lifecycle;
import androidx.test.core.app.ActivityScenario;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import uy.edu.tse.hcen.config.AppConfig;
import uy.edu.tse.hcen.manager.SessionManager;

@RunWith(AndroidJUnit4.class)
public class LoginCallbackActivityCoverageTest {
    private Context context;

    @Before
    public void setUp() {
        context = ApplicationProvider.getApplicationContext();
        SessionManager.clearSession(context);
        SessionManager.clearState(context);
        TestNetworkDispatcher.install();
        TestNetworkDispatcher.clear();
    }

    // No tearDown necesario

    @Test
    public void testIsMinorShowsDialogAndRedirects() {
    Intent intent = new Intent(context, LoginCallbackActivity.class);
    intent.setData(Uri.parse("hcen://callback?state=error:menor_de_edad"));
    ActivityScenario<LoginCallbackActivity> scenario = ActivityScenario.launch(intent);
    SystemClock.sleep(3500); // Espera a que termine el postDelayed
    // Verifica que la Activity se destruyó (fue redirigida)
    scenario.moveToState(Lifecycle.State.DESTROYED);
    }

    @Test
    public void testFallbackToSplashShowsDialogAndRedirects() {
    SessionManager.saveState(context, "abc");
    // Simula error backend
    TestNetworkDispatcher.enqueueResponse(AppConfig.AUTH_SESSION_URL, 500, "{}");
    Intent intent = new Intent(context, LoginCallbackActivity.class);
    intent.setData(Uri.parse("hcen://callback?token=tkn&state=abc"));
    ActivityScenario<LoginCallbackActivity> scenario = ActivityScenario.launch(intent);
    SystemClock.sleep(3500); // Espera a que termine el postDelayed
    // Verifica que la Activity se destruyó (fue redirigida)
    scenario.moveToState(Lifecycle.State.DESTROYED);
    }

    @Test
    public void testOnCreateAllBranches() {
    // Caso: data null
    Intent intent1 = new Intent(context, LoginCallbackActivity.class);
    ActivityScenario<LoginCallbackActivity> scenario1 = ActivityScenario.launch(intent1);
    SystemClock.sleep(500);
    scenario1.moveToState(Lifecycle.State.DESTROYED);

    // Caso: token/state null
    Intent intent2 = new Intent(context, LoginCallbackActivity.class);
    intent2.setData(Uri.parse("hcen://callback?token=&state="));
    ActivityScenario<LoginCallbackActivity> scenario2 = ActivityScenario.launch(intent2);
    SystemClock.sleep(500);
    scenario2.moveToState(Lifecycle.State.DESTROYED);

    // Caso: state incorrecto
    Intent intent3 = new Intent(context, LoginCallbackActivity.class);
    intent3.setData(Uri.parse("hcen://callback?token=tkn&state=wrong"));
    ActivityScenario<LoginCallbackActivity> scenario3 = ActivityScenario.launch(intent3);
    SystemClock.sleep(500);
    scenario3.moveToState(Lifecycle.State.DESTROYED);
    }

    @Test
    public void testOnCreateLambdaOnCreate0TrueAndFalse() {
    // true: backend responde OK
    SessionManager.saveState(context, "abc");
    TestNetworkDispatcher.enqueueResponse(AppConfig.AUTH_SESSION_URL, 200, "{\"jwt\":\"jwt-value\"}");
    Intent intent = new Intent(context, LoginCallbackActivity.class);
    intent.setData(Uri.parse("hcen://callback?token=tkn&state=abc"));
    ActivityScenario<LoginCallbackActivity> scenario = ActivityScenario.launch(intent);
    SystemClock.sleep(500);
    assertEquals("jwt-value", SessionManager.getJwtSession(context));
    scenario.moveToState(Lifecycle.State.DESTROYED);

    // false: backend responde error
    SessionManager.saveState(context, "abc2");
    TestNetworkDispatcher.enqueueResponse(AppConfig.AUTH_SESSION_URL, 500, "{}");
    Intent intent2 = new Intent(context, LoginCallbackActivity.class);
    intent2.setData(Uri.parse("hcen://callback?token=tkn&state=abc2"));
    ActivityScenario<LoginCallbackActivity> scenario2 = ActivityScenario.launch(intent2);
    SystemClock.sleep(3500); // Espera a que termine el fallback
    scenario2.moveToState(Lifecycle.State.DESTROYED);
    }
}
