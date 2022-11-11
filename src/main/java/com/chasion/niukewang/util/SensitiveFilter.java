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
        while (start < len){
            if (pos < len){
                // 过滤字符
                char c = text.charAt(pos);
                if (isSymbol(c)){
                    // 保留开始的特殊字符
                    if (cur == rootNode){
                        after.append(c);
                        start++;
                    }
                    pos++;
                    continue;
                }
                // 检查下级节点
                cur = cur.getSubNode(c);
                if (cur == null){
                    // 在前缀树中没有找到当前字符
                    // 以start开头的字符串不是敏感词
                    // 如：敏感词是 a b c
                    // 此时情况是   a a b
                    // 不能构成完整了敏感词，说明以a开始头的词不是敏感词，则添加a即可，再从第二个a从新开始判断
                    after.append(text.charAt(start));
                    pos = ++start;
                    cur = rootNode;
                }else if (cur.isKeywordEnd()){
                    // 如果找到的正好是敏感词结尾，从start到pos是个完整的敏感词
                    after.append(REPLACEMENT);
                    // a b c
                    // a b c d e f
                    start = ++pos;
                    cur = rootNode;

                }else {
                    // 此时在敏感词中间位置，未到达结尾，继续往下遍历
                    pos++;
                }

            }else {
                // pos 遍历越界仍未匹配到敏感词
                // 此时是尾指针越界，头指针还没越界
                // 例如 f a b c
                // start 在 f   pos在c
                // fabc不是以f开头的敏感词，过滤不掉而此时pos也到了末尾，跳出循环结束了
                // 没有判断从start到pos子串的情况
                // 所以，以start作为while循环判断的标准，遍历完子串情况
                after.append(text.charAt(start));
                pos = ++start;
                cur = rootNode;
            }
        }
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
