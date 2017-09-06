#import "JackReader.h"
#import <AVFoundation/AVAudioSession.h>


@implementation JackReader

iMagRead * _reader;


- (void)ReadCard:(CDVInvokedUrlCommand *)command {
   
    _callbackID = command.callbackId;
    _reader = [[iMagRead alloc]init];
    
    if([self isHeadsetPluggedIn]){
       [_reader Start];
   
        [self.commandDelegate runInBackground:^{
    
                NSString * CARREAD_MSG_ByteUpdate = @"CARREAD_MSG_ByteUpdate";
        
                [[NSNotificationCenter defaultCenter] addObserver:self selector:@selector(UpdateBytes:) name:CARREAD_MSG_ByteUpdate object:nil ];
        }];
    }else{
        CDVPluginResult* pluginResult = nil;
        pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK messageAsString:@"No Swiper"];
        [self.commandDelegate sendPluginResult:pluginResult callbackId:command.callbackId];
        
        NSLog(@"Location%@",@"No Swiper Attached");
    
    }
}


- (BOOL)isHeadsetPluggedIn
{
    AVAudioSessionRouteDescription *route = [[AVAudioSession sharedInstance] currentRoute];
    
    BOOL headphonesLocated = NO;
    for( AVAudioSessionPortDescription *portDescription in route.outputs )
    {
        headphonesLocated |= ( [portDescription.portType isEqualToString:AVAudioSessionPortHeadphones] );
    }
    return headphonesLocated;
}

- (void) UpdateBytes:(NSNotification*)aNotification{
    NSString* str = [aNotification object];
    [self performSelectorOnMainThread:@selector(updateBytCtl:) withObject:str waitUntilDone:NO];
}

- (void) updateBytCtl:(NSString*) text;
{
    
    //NSLog(@"Location%@",text);
    CDVPluginResult* pluginResult = nil;
    [[NSNotificationCenter defaultCenter] removeObserver:self];
    if([text length] > 30){
        //NSLog(@"Location%@",@"Success Case");
        NSArray *CardDetails = [text componentsSeparatedByString: @"="];
        
        NSString* Cardinfo = [CardDetails objectAtIndex: 0];
        
        NSString* CardNumber = [Cardinfo substringWithRange:NSMakeRange(1, [Cardinfo length]-1)];
        
        NSString* expYear = @"20";
        
        NSString* expYearSort = [[CardDetails objectAtIndex: 1] substringWithRange:NSMakeRange(0,2)];
        
        expYear = [expYear stringByAppendingString:expYearSort];
        NSString* expMonth = [[CardDetails objectAtIndex: 1] substringWithRange:NSMakeRange(2,2)];
        
        NSDictionary* response = [NSDictionary dictionaryWithObjects:@[CardNumber,expMonth,expYear]
                                                             forKeys:@[@"card_numer",@"expiry_month",@"expiry_year"]];
        NSMutableArray *array1 = [[NSMutableArray alloc] init];
        NSMutableDictionary *dict1 = [NSMutableDictionary dictionaryWithObjectsAndKeys:CardNumber,@"card_number",expMonth,@"expiry_month",expYear,@"expiry_year",nil];
        [array1 addObject:dict1];
        NSCharacterSet* notDigits = [[NSCharacterSet decimalDigitCharacterSet] invertedSet];
    
        if (response != nil && [response count] > 0  && [expMonth length] == 2 && [expYear length] == 4 && [CardNumber length] > 13 && [CardNumber rangeOfCharacterFromSet:notDigits].location == NSNotFound && [expYear rangeOfCharacterFromSet:notDigits].location == NSNotFound && [expMonth rangeOfCharacterFromSet:notDigits].location == NSNotFound) {
            
            pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK messageAsArray:array1];
            [self.commandDelegate sendPluginResult:pluginResult callbackId:_callbackID];
            
        } else {
            pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK messageAsString:@"error"];
            [self.commandDelegate sendPluginResult:pluginResult callbackId:_callbackID];
            
        }
        
    }else{
        pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK messageAsString:@"error"];
        [self.commandDelegate sendPluginResult:pluginResult callbackId:_callbackID];
        
        NSLog(@"Location%@",@"Error Case");
    }
 [_reader Stop];
}

@end
