package musicstore.pages

import geb.Module

class AuthModule extends Module {
	
	static content = {
		loggedInMessage(required: false) { $("#loggedInUser").text() }
		loginLink(required: false, to: LoginPage) { $("#loginLink a") }
	}
	
	boolean isLoggedIn() {
		loginLink.empty
	}

}