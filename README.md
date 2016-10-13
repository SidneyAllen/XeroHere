# XeroHere 
Example code for Android PayPal Here integration with Xero API private application type. This will allow an in person payment and to create an invoice, it is also possible to use this example and do the reverse and even selecting an invoice to be able to do a transaction on. 

*It is important to note that this is a proof of concept and the invoice creation method has been tied to `onPaymentFailure` for testing reasons only and should be moved to `onPaymentSuccess` before using in a live environment.*

This has sample keys in it which have been invalidated. You will need to create your own keys and Xero application to use it which you can do via [developer.xero.com](https://developer.xero.com)

For more on Xero private integration visit the [Private Applications](https://developer.xero.com/documentation/getting-started/private-applications/) page.

For more info on generating your own private keys visit the [Private Keypair](https://developer.xero.com/documentation/advanced-docs/public-private-keypair/ ) page.

You will also need a PayPal Here device and your PayPal account authorised to use [PayPal Here](https://paypal.com/here), please note that there is no sandbox environment for PayPal Here so you will need to develop in a live environment. 

# Reference git repos
This has been built as a combination of 2 repos
- [Xero Private Android Sample](https://github.com/coderbec/Xero-Private-Sample) 
- [PayPal Here Android SDK](https://github.com/PayPal-Mobile/android-here-sdk-dist)

# Instructions

Open /app/src/main/res/values/strings.xml and add 2 tags for api_key and api_secret from your private auth settings.

`<string name="api_key">ABCDEFGHIJKLMNOPQRSTUVWXYZ</string>`
`<string name="api_secret">ABCDEFGHIJKLMNOPQRSTUVWXYZ</string>`

Open /app/src/main/java/com/becmartin/xeroandroidprivate/XeroAPI.java and update the PrivateKey getPrivateKey() string with the key the private auth key remembering to make sure the key lines are split with a `\n`
