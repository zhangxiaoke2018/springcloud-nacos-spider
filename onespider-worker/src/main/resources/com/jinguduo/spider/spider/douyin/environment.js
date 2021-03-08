function _sign(userId, tac, userAgent) {
	var window = {
		CanvasRenderingContext2D: 1,
		Audio: 1,
	    navigator: {
			userAgent: userAgent
		},
		tac: tac,
        canvas: {
	    	width: 48,
	    	height: 16,
	    	getContext: function(s) {
	    		return {
	    			font: "14px serif",
	    			shadowBlur: 2,
	    			shadowOffsetX: 1,
	    			shadowOffsetY: 0,
	    			shadowColor: "#00ff00",
	    			stroke: function() {},
	    			arc: function() {},
	    			fillText: function(t, w, h) { /* 龘ฑภ경 */ },
	    		};
	    	},
        },
    	toDataURL: function() { return "data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAADAAAAAQCAYAAABQrvyxAAAEDElEQVRIS73We4hVVRQG8N+5OjZqKRq9qBDNHlQaiZUp9DLR7EHauyjDLCKSQssI648o0+hBmWZZRA9MrcweWllaDvSESUPUJCjT0kKsUCtfM3NjHfeJ6/WO/aULhjl377P3Wd/6vvXtndkzMhyHNdiWptsgxptqvL8/htphBw7HQ7gYJ+DPSKoy4sUWjE0vj8NVaMQ0DE7zscHn2LAPsj8H7+IzbEmFuwKPIvIp4Tv0qwXgSpyeADyPmzEXH+HatGm39HwePt0HAGLL7on9KFBXLMfkVLSemFCLgaDnGpyKS3EfnqpIMDabgqtRj+MrJBUSa0YddlasOQDbWwHZMa0pZNpaLW7FA3gN6/FzyqumhDphKT5MyA/C6rT4bSxGsDQeD6cvRrW+wkuJ4svwJRbixKTXeRXZDUqMFkMLcBPW1UAQRf01Fev7NL9XCUVlv8GKtLA3ZiRAMR4gzsebmI1lSsYpm6AsPhDUvpw+NCex+QSmprFRCGlehE9wCJ7FMTi5ir1IdBHewsfojPboktbUZCBoDc0PxbnpxUfwAdbidfyWazRzhk4usNPtyjbZobfMg5ryvngRkewzWImnE4AxeDw9n5bM4ZIEKk8ozYW5vJGa9X70QTR3qCNiVmsudFTSWFTmTLyQqhVJB4C7MFLJLO2s8Yp55uvrfQtsN83fOmg2EMFcJBPr46MFA9Ej32ISXk3JXJ7mCwBtEbKaj2CvMo7GL/gCF+KPShuNhaHxAWlFNO2hycoCcTjSrohV9TZob7G+1llghjG2mWK5FkM154xFVAMIiYY8o08CSEQlgDCC6J0wkHC+IoYjJBkR/2cWvysBRPVHp0YsFob33o078eR/27U1T0cnabLVRDONttj1WszOz4a+duZ9VA0gKlY0cyGfagCbaxyYIefnEM2/CpFnmEQYxJZaDEQScWAUEW7zEw7OF2WO1VYPR2iw1nBbjVJvuWHK3tOoWWVyBQORQNAeDRsOdl1yrmoARQ9UfN5E/JDYLPgPAwgWd5NQTN6RzoBomDgFw0ZjLAC8I7NFncfUu1FnqxyobKXpuSz6a+9ry7Q4JXenPRm4LWk9LDeq+VcNCdUCELmE4/VP+u+V+iMOtN0YiP0mK+X3n6A7qj1X2QAlM2W5fQ3R1XSd9POjgZYaqVeu6TgrNuUHW6ZcWb6q5w74Zy/zrU3FFSbOpojozQuwpKCjWJQpmaSdFeqcpYsGm9ygrEmLAQ4zVpPBelqi0Xh9bDbCPUbkXv17ftnK8nvUfo3dL3NlJWfrpsFWxKm8a36dekfKDDHMImNMda9bcnsMS9uY31uy/Cqx36MaQBs9DbLaHL0tVCezQ731etiouz4aNOb3o7iTxF9oNu46Lf8jnX0G7F+AsSkgsWDREQAAAABJRU5ErkJggg=="},
	};
	var document = {
		createElement: function(s) {return window.canvas},
		getElementById: function(s) {return {}},
	};
	return _signature(userId);
};