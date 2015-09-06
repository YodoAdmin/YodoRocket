# README #

### What is this repository for? ###

The YodoLauncher, basically an android application that can be called from external apps to realize payments.

### How do I get set up? ###

* To call the launcher:

```
#!java

 Intent test = new Intent( "co.yodo.launcher.POS" );
 test.putExtra( "TOTAL", "25.00" );
 test.putExtra( "CASH_TENDER", "43.25" );
 test.putExtra( "CASH_BACK", "10.50" );
 test.putExtra( "PROMPT_RESPONSE", false );
 startActivityForResult( test, 0 );

```

* To get the results:

```
#!java


  public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        if( requestCode == 0 ) {
            if( resultCode == RESULT_OK ) {
                String code       = intent.getStringExtra("RESULT_CODE");
                String authNumber = intent.getStringExtra("RESULT_AUTH");
                String message    = intent.getStringExtra("RESULT_MSG");
                // Handle successful transaction
                Toast.makeText( this, code + " - " + authNumber + " - " + message, Toast.LENGTH_LONG ).show();
            } else if ( resultCode == RESULT_CANCELED ) {
                // Handle cancel
            }
        }
    }

```