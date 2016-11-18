package com.cssru.textsearch;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

public class Main {
	private static final int VOID = -1;
	
	public static void main(String ... args) {
		if (args.length < 3) {
			printUsage();
		}
		
		char leftDelimiter = args[1].charAt(0);
		char rightDelimiter = args[2].charAt(0);
		String text = args[0];
		int blockStart = VOID;
		List<TextBlock> blocks = new LinkedList<>();
		
		for (int i = 0; i < args[0].length(); i++) {
			if (text.charAt(i) == leftDelimiter) {
				blockStart = i;
			} else if (text.charAt(i) == rightDelimiter && blockStart != VOID) {
				String blockText = text.substring(blockStart + 1, i);
				if (!blockText.isEmpty()) {
					blocks.add(new TextBlock(blockText));
				}
				blockStart = VOID;
			}
		}
		
		CommonStatistics commonStatistics = createCommonStatistics(blocks);
		List<BlockStatistics> blockStatisticsList = new LinkedList<>();
		for (TextBlock block : blocks) {
			blockStatisticsList.add(createBlockStatistics(block));
		}
		
		JsonFactory jsonFactory = new JsonFactory();
		try {
			JsonGenerator generator = jsonFactory.createGenerator(new PrintWriter(System.out));
			generator.setCodec(new ObjectMapper());
			
			printCommonStatistics(commonStatistics, generator);
			
			for(BlockStatistics block : blockStatisticsList) {
				printBlockStatistics(block, generator);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private static CommonStatistics createCommonStatistics(List<TextBlock> blocks) {
		CommonStatistics result = new CommonStatistics();
		
		result.setTotalBlocks(blocks.size());
		result.setAvgBlockLength(getAvgBlockLength(blocks));
		result.setMaxBlockLength(getMaxBlockLength(blocks));
		result.setMinBlockLength(getMinBlockLength(blocks));
		
		return result;
	}
	
	private static BlockStatistics createBlockStatistics(TextBlock block) {
		BlockStatistics result = new BlockStatistics();
		
		int latinCount = getLatinCount(block.getText());
		int cyrCount = getCyrCount(block.getText());
		int cypherCount = getCypherCount(block.getText());
		int otherSymCount = block.getText().length() - latinCount - cyrCount - cypherCount;

		result.setText(block.getText());
		result.setTextLength(block.getText().length());
		result.setLatinCount(latinCount);
		result.setCyrCount(cyrCount);
		result.setCypherCount(cypherCount);
		result.setOtherSymCount(otherSymCount);
		
		return result;
	}
	
	private static void printUsage() {
		System.out.println("Usage: jar-file <text to analyze> <left block delimiter> <right block delimiter>");
	}
	
	private static void printCommonStatistics(CommonStatistics stats, JsonGenerator generator) throws IOException {
		generator.writeObject(stats);
		System.out.println();
	}
	
	private static void printBlockStatistics(BlockStatistics stats, JsonGenerator generator) throws IOException {
		generator.writeObject(stats);
		System.out.println();
	}
	
	private static int getLatinCount(String text) {
		return (int)text
			.chars()
			.filter(c ->
				'a' <= c && c <= 'z'
				|| 'A' <= c && c <= 'Z'
			)
			.count();
	}
	
	private static int getCyrCount(String text) {
		return (int)text
			.chars()
			.filter(c ->
				'а' <= c && c <= 'я'
				|| 'А' <= c && c <= 'Я'
			)
			.count();
	}
	
	private static int getCypherCount(String text) {
		return (int)text
			.chars()
			.filter(Character::isDigit)
			.count();
	}
	
	private static int getAvgBlockLength(List<TextBlock> blocks) {
		return (int)Math.round(
			Optional.ofNullable(blocks)
			.orElse(Collections.emptyList())
			.stream()
			.mapToInt(b -> b.getText().length())
			.average()
			.orElseGet(() -> 0)
		);
	}
	
	private static int getMaxBlockLength(List<TextBlock> blocks) {
		return Optional.ofNullable(blocks)
			.orElse(Collections.emptyList())
			.stream()
			.mapToInt(b -> b.getText().length())
			.max()
			.orElseGet(() -> 0);
	}
	
	private static int getMinBlockLength(List<TextBlock> blocks) {
		return Optional.ofNullable(blocks)
			.orElse(Collections.emptyList())
			.stream()
			.mapToInt(b -> b.getText().length())
			.min()
			.orElseGet(() -> 0);
	}
	
	@RequiredArgsConstructor
	@Getter
	private static class TextBlock {
		private final String text;
	}
	
	@Getter
	@Setter
	private static class CommonStatistics {
		private int totalBlocks;
		private int avgBlockLength;
		private int maxBlockLength;
		private int minBlockLength;
	}
	
	@Getter
	@Setter
	private static class BlockStatistics {
		private String text;
		private int textLength;
		private int latinCount;
		private int cyrCount;
		private int cypherCount;
		private int otherSymCount;
	}
}
