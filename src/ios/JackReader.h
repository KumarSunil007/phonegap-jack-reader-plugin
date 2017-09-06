#import <Cordova/CDV.h>
#import "iMagRead.h"

//@interface ViewController : UIViewController{iMagRead * _reader;}

@interface JackReader : CDVPlugin {
    NSString* callbackID;
}

/*{
	iMagRead * _reader;
 NSString* callbackID;
 }*/
@property (nonatomic, copy, readwrite) NSString *callbackID;
@property NSString* CardData;
@property NSDictionary *response;

- (void)ReadCard:(CDVInvokedUrlCommand *)command;


@end
