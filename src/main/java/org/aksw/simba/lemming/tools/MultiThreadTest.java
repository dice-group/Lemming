package org.aksw.simba.lemming.tools;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class MultiThreadTest {
	
	public static Integer powersOf(int i, int pow) {
		
		return (int) Math.pow(i, pow);
	}
	
	public static void main(String[] args) {
		for(int i=0; i<5; i++) {
			
			CompletableFuture<Integer> future1  
			  = CompletableFuture.supplyAsync(() -> powersOf(2,2));
			CompletableFuture<Integer> future2  
			  = CompletableFuture.supplyAsync(() -> powersOf(3,2));
			CompletableFuture<Integer> future3  
			  = CompletableFuture.supplyAsync(() -> powersOf(5,2));
			
			CompletableFuture<Void> combinedFuture 
			  = CompletableFuture.allOf(future1, future2, future3);
			
			combinedFuture.join();
			
			System.out.println(future1.isDone());
			System.out.println(future2.isDone());
			System.out.println(future3.isDone());
			
			List<Integer> combined = Stream.of(future1, future2, future3)
					  .map(CompletableFuture::join)
					  .collect(Collectors.toList());
			
			System.out.println(combined.get(0));
			System.out.println(combined.get(1));
			System.out.println(combined.get(2));
		}

	}
}
