var exec = require('cordova/exec');


var JackReader = function(){};									
    JackReader.prototype.ReadCard = function(successCallback, errorCallback){
    											if (errorCallback == null) { errorCallback = function() {};}	
												exec(successCallback, errorCallback, "JackReader", "ReadCard", []);
											};																					
    module.exports = new JackReader();

