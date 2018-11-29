# team_senior

## How to read user's settings

```java
/* make sure to put "import android.content.SharedPreferences;" in your file */

SharedPreferences prefs = getSharedPreferences("ga_preferences", MODE_PRIVATE);

/* to get the response time, if no response time saved then default to 30 */
int a = prefs.getInt("responseTime", 30);
/* to get the boolean storing if the user wants the app to enabled */
boolean b = prefs.getBoolean("enabled", true);
/* to get the boolean storing if the user wants haptic feedback */
boolean c = prefs.getBoolean("hapticEnabled", true);
```
