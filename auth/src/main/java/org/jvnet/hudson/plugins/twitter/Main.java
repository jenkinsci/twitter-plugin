package org.jvnet.hudson.plugins.twitter;

import java.io.BufferedReader;
import java.io.InputStreamReader;

import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.auth.AccessToken;
import twitter4j.auth.RequestToken;

public class Main {
	private static final String CONSUMER_KEY = "8B6nAb0a5QScWxROd5oWA";;
	private static final String CONSUMER_SECRET = "pXO0lgCZYUvix7Ay7YLdsIep38VBiH2cTldOeMj1J5s";
	

	public static void main(String[] args) throws Exception {
		Twitter twitter = new TwitterFactory().getInstance();
		twitter.setOAuthConsumer(CONSUMER_KEY, CONSUMER_SECRET);
		RequestToken requestToken = twitter.getOAuthRequestToken();
		AccessToken accessToken = null;
		BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
		while (null == accessToken) {
			System.out.println("Open the following URL and grant access to your account:");
			System.out.println(requestToken.getAuthorizationURL());
			System.out.print("Enter the PIN(if aviailable) or just hit enter.[PIN]:");
			String pin = br.readLine();
			try{
				if(pin.length() > 0){
					accessToken = twitter.getOAuthAccessToken(requestToken, pin);
				}else{
					accessToken = twitter.getOAuthAccessToken();
				}
			} catch (TwitterException te) {
				if(401 == te.getStatusCode()){
					System.out.println("Unable to get the access token.");
				}else{
					te.printStackTrace();
				}
				System.exit(1);
			}
		}
		storeAccessToken(twitter.verifyCredentials().getId() , accessToken);
		System.exit(0);
	}
	private static void storeAccessToken(long useId, AccessToken accessToken){
		System.out.println("access token:" + accessToken.getToken());
		System.out.println("access token secret:" + accessToken.getTokenSecret());
	}
}
