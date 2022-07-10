package com.chasion.niukewang.util;

import jdk.nashorn.internal.ir.Symbol;
import org.apache.commons.lang3.CharUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

/**
 * @ClassName SensitiveFilter
 * @Description TODO
 * @Author chasion
 * @Date 2022/6/29 20:34
 * 过滤敏感词
 */
@Component
public class SensitiveFilter {
    private static final Logger logger = LoggerFactory.getLogger(SensitiveFilter.class);
    // 替换符号
    private static final String REPLACEMENT = "***";
    // 创建根节点
    private TrieNode rootNode = new TrieNode();

    @PostConstruct
    public void init(){
        // 加载敏感词文件
        try(
                InputStream is = this.getClass().getClassLoader().getResourceAsStream("sensitive-words.txt");
                // 缓冲流
                BufferedReader reader = new BufferedReader(new InputStreamReader(is));
        ) {
            String keyword;
            while ((keyword = reader.readLine()) != null){
                // 添加到前缀树
                this.addKeyword(keyword);
            }
        }catch (IOException e){
            logger.error("加载敏感词文件失败：" + e.getMessage());
        }

    }

    // 将一个敏感词添加到前缀树
    private void addKeyword(String keyword){
        TrieNode tempNode = rootNode;
        for (int i = 0; i < keyword.length(); i++) {
            char c = keyword.charAt(i);
            // 首先查找这个字符是否在前缀树中存在
            TrieNode subNode = tempNode.getSubNode(c);
            if (subNode == null){
                // 如果不存在，初始化，添加
                subNode = new TrieNode();
                tempNode.addSubNode(c, subNode);
            }
            // 指针后移
            tempNode = subNode;
            if (i == keyword.length() - 1){
                tempNode.setKeywordEnd(true);
            }
        }
    }


    // 检索是否含有敏感词
    public String filter(String text){
        int len = text.length();
        if (len == 0){
            return null;
        }
        int start = 0;
        int pos = 0;
        TrieNode cur = rootNode;
        StringBuilder after = new StringBuilder();
        while (pos < len){
            // 遍历从start到pos之间的字符，如果是敏感词，则替换，如果不是，则start后移，进入下一轮判断
            char c = text.charAt(pos);
            // 跳过符号
            if (isSymbol(c)){
                if (cur == rootNode){
                    after.append(c);
                    start++;
                }
                pos++;
                continue;
            }

            // 判断是否敏感
            // 分三种情况
            // 1、从start 到 pos不是敏感词
            cur = cur.getSubNode(c);
            if (cur == null){
                // 添加start，然后指针后移，继续判断
                after.append(text.charAt(start));
                pos = ++start;
                cur = rootNode;
            }else if (cur.isKeywordEnd()){
                // 发现是敏感词，匹配完成
                after.append(REPLACEMENT);
                // 直接跳过敏感词，开始接下来的判断
                start = ++pos;
                cur = rootNode;
            }else {
                // 检查下一个
                pos++;
            }

        }
        // 跳出循环是pos在末尾，然后再加+，不满足条件
        // 也就说，不满足三种情况中的前两种，也就是不完全是敏感词
        // 如 a b c  -- a  b  d
        after.append(text.substring(start));
        return after.toString();
    }

//    public void display(){
//        System.out.println(rootNode.getSubNode('操').isKeywordEnd());
//    }

    // 判断是否是特殊符号
    private boolean isSymbol(Character character){
        // 字符合法性判断，跳过符号，0x2E80 0x9FFF是东亚文字范围
        return !CharUtils.isAsciiAlphanumeric(character) && (character < 0x2E80 || character > 0x9FFF);
    }

}

// 前缀树
class TrieNode{

    // 关键词结束标识
    private boolean isKeywordEnd = false;

    // 子节点
    private Map<Character, TrieNode> subNodes = new HashMap<>();

    public boolean isKeywordEnd() {
        return isKeywordEnd;
    }

    public void setKeywordEnd(boolean keywordEnd) {
        isKeywordEnd = keywordEnd;
    }

    public Map<Character, TrieNode> getSubNodes() {
        return subNodes;
    }

    public void setSubNodes(Map<Character, TrieNode> subNodes) {
        this.subNodes = subNodes;
    }

    // 添加子节点
    public void addSubNode(Character c, TrieNode node){
        subNodes.put(c, node);
    }

    // 获取子节点
    public TrieNode getSubNode(Character character){
        return subNodes.get(character);
    }
}
