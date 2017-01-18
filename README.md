# Jack-Reader
This plugin is used for reading data from magnetic card with the help of 3.5 mm headphone jack card reader. Currently this is working perfectly for the Android. I am working for IOS also, when I will finshed with it, you can get the same from this repository

## Usages:
Create a button in HTML file.
```html

    <button type="button" id="swipe-btn">Swipe</button>
    
```


## To read card data use following following function ( use jQuery as well) : 

    jQuery('#swipe-btn').click(function(){
      com.deftsoft.org.jack.reader.JackReader.ReadCard(SwipeResult);
    }
    
    
    function SwipeResult(resp){
	    if(resp.card_number){
		console.log('Card Number:'+ resp.card_number +
			    '\nExpiry Month:'+ resp.expiry_month +
			    '\nExpiry Year:'+ resp.expiry_year
			    );
		}else{
			console.log('Error: Card not swiped successfully');
		}
    }
    
If you have any problem related to plugin Integration please let me know.    
    
