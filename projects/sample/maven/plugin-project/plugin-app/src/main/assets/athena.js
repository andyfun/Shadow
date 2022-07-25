function athena_getSDKData(callback){
              callbackSDKData = function(json){
               callback(json)
            } ;

            AthenaH5Sdk.athena_getSDKData();
 }

 function athena_closeBrowser(){
    AthenaH5Sdk.athena_closeBrowser();
 }

 function athena_log(data){
        AthenaH5Sdk.athena_log(data);
 }
 function athena_share(data,callback){
           callbackShareData = function(json){
                callback(json)
           };
            AthenaH5Sdk.athena_share(data);
       }

