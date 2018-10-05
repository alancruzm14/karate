function() {

	var env = karate.env; // get java system property 'karate.env'
	karate.log('karate.env system property es:', env);
	
	karate.configure('connectTimeout', 10000);
	karate.configure('readTimeout', 10000); 
	return {};
}
