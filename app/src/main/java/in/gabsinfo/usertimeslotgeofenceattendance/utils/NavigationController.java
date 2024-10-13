package in.gabsinfo.usertimeslotgeofenceattendance.utils;

import android.content.Context;

import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;

import in.gabsinfo.usertimeslotgeofenceattendance.R;

public class NavigationController {
    private NavHostFragment navHostFragment;
    private static NavigationController navigationController;
    private Context context;

    public static NavigationController getInstance() {
        if (navigationController == null) {
            navigationController = new NavigationController();
        }
        return navigationController;
    }

    public void initialize(AppCompatActivity context) {
        this.context = context;
        navHostFragment = (NavHostFragment) context.getSupportFragmentManager().findFragmentById(R.id.fragment_container_view);
    }

    public NavController getNavController() {
        return navHostFragment.getNavController();
    }

}
