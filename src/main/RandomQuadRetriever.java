package main;

import java.util.ArrayList;
import java.util.Collections;
import java.util.concurrent.ThreadLocalRandom;

import enums.ComponentType;
import exceptions.NoMoreRandomNumbersException;
import exceptions.NotEnoughPossibilitiesException;
import exceptions.ZeroPossibilitiesException;
import util.ComponentQuad;
import util.ComponentString;

public class RandomQuadRetriever {
	
	private boolean[] needed = new boolean[4];
	private ComponentQuad componentQuad = new ComponentQuad();
	
	// only needed if there is only one value needed (3 random values)
	private ArrayList<Long> randomNumbers;
	private int randomNumberPointer = 0;

	public RandomQuadRetriever(String pattern) throws NotEnoughPossibilitiesException {
		for(int i = 0; i < 4; i++) {
			needed[i] = pattern.charAt(i) != '?';
		}
		if(!enoughPossibilities(pattern)) {
			throw new NotEnoughPossibilitiesException();
		}
	}
	
	public void reset() {
		componentQuad = new ComponentQuad();
	}

	private boolean enoughPossibilities(String pattern) {
		return QueryTester.virtuosoTest.moreRecordsThan(pattern, Go.MIN_NUMBER_OF_QUERIES);
	}

	public ComponentQuad getNextRandomQuad() throws NotEnoughPossibilitiesException, ZeroPossibilitiesException, NoMoreRandomNumbersException {
		for(int i = 0; i < 4; i++) {
			if(needed[i]) {
				componentQuad.setComponent(getRandomComponent(ComponentType.getComponent(i)));
			}
		}
		return componentQuad;
	}

	private ComponentString getRandomComponent(ComponentType type) throws NotEnoughPossibilitiesException, ZeroPossibilitiesException, NoMoreRandomNumbersException {
		long possibilities = getNumberOfPossibilities(type);
		if(possibilities == 0) {
			throw new ZeroPossibilitiesException();
		}
		long randomNumber = getNextRandomNumber(0,possibilities-1);
		return QueryTester.getRecord(type, randomNumber, componentQuad);
	}
	
	private long getNextRandomNumber(long min, long max) throws NoMoreRandomNumbersException {
		// if there is more than 1 value needed, we just return any random number
		if(getNeededValuesCount() > 1) {
			return randLong(min,max);
		} else {
			// if there is exactly 1 value needed, one random number after the other is returned
			if(randomNumbers == null) {
				randomNumbers = new ArrayList<Long>();
				for(long i = min; i <= max; i++) {
					randomNumbers.add(i);
				}
				Collections.shuffle(randomNumbers);
			}
			if(randomNumberPointer >= randomNumbers.size()) {
				throw new NoMoreRandomNumbersException();
			}
			return randomNumbers.get(randomNumberPointer++);
		}
		
	}
	
	private long getNumberOfPossibilities(ComponentType type) throws NotEnoughPossibilitiesException {
		if(allEmpty()) {
			long possibilities = getPossibilitiesFromHDT(type);
			if(possibilities <= Go.MIN_NUMBER_OF_QUERIES && getNeededValuesCount() == 1) {
				throw new NotEnoughPossibilitiesException();
			} else {
				return possibilities;
			}
		}
		return getPossibilitiesFromVirtuoso(type);
	}

	private int getNeededValuesCount() {
		int count = 0;
		for(int i = 0; i < needed.length; i++) {
			if(needed[i]) {
				count++;
			}
		}
		return count;
	}

	/**
	 * HDT returns distinct elements for given component
	 */
	private long getPossibilitiesFromHDT(ComponentType component) {
		switch(Go.HDT_TYPE) {
		case HDT_AG:
			return QueryTester.hdtAGTest.getNumberOfDistinctElements(component);
		case HDT_AT:
			return QueryTester.hdtATTest.getNumberOfDistinctElements(component);
		}
		throw new RuntimeException("hdttype is not valid" + Go.HDT_TYPE);
	}
	
	/**
	 * Virtuoso returns distinct elements for the given component, given that one or more other components are set
	 * @param i
	 * @return
	 */
	private long getPossibilitiesFromVirtuoso(ComponentType type) {
		return QueryTester.virtuosoTest.getNumberOfDistinctElements(type, componentQuad);
	}

	private boolean allEmpty() {
		ComponentQuad emptyQuad = new ComponentQuad();
		return emptyQuad.equals(componentQuad);
	}
	/**
	 * Returns a pseudo-random number between min and max (incl.).
	 */
	private long randLong(long min, long max) {
	    return ThreadLocalRandom.current().nextLong(max + 1);
	}
}
