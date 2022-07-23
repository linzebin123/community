package com.nowcoder.community.utils;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.CharUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

@Component
@Slf4j
public class SensitiveFilter {

    //替换符
    public static final String REPLACEMENT="***";
    //根节点
    private TrieNode root=new TrieNode();

    @PostConstruct
    public void init(){
        try (
                InputStream is = this.getClass().getClassLoader().getResourceAsStream("sensitive-words.txt");
                BufferedReader reader=new BufferedReader(new InputStreamReader(is));
                ){
                String keyword;
                while ((keyword=reader.readLine())!=null){
                    //将关键词添加到前缀树
                    this.addKeyword(keyword);
                }
        }catch (IOException e){
            log.error("加载敏感词文件失败："+e.getMessage());
        }

    }

    private void addKeyword(String keyword) {
        TrieNode tempNode=root;
        for (int i = 0; i < keyword.length(); i++) {
            char c=keyword.charAt(i);
            TrieNode subNode = tempNode.getSubNode(c);
            if (subNode==null){
                //初始化子节点
                subNode=new TrieNode();
                tempNode.setSubNode(c,subNode);

            }
            //指向子节点，指向下一轮循环
            tempNode=subNode;
            if (i==keyword.length()-1){
                tempNode.setKeywordEnd(true);
            }
        }
    }

    //前缀树结构
    private class TrieNode{
        //关键词结束标识
        private boolean isKeywordEnd=false;
        //子节点
        private Map<Character,TrieNode> subNode=new HashMap<>();

        public boolean isKeywordEnd() {
            return isKeywordEnd;
        }

        public void setKeywordEnd(boolean keywordEnd) {
            isKeywordEnd = keywordEnd;
        }

        //添加子节点

        public void setSubNode(Character c,TrieNode node) {
            subNode.put(c,node);
        }

        //获取子节点

        public TrieNode getSubNode(Character c) {
            return subNode.get(c);
        }
    }
    public String filter(String text){
        if (StringUtils.isBlank(text)){
            return null;
        }
        //指针1指向前缀树
        TrieNode tempNode=root;
        //指针2
        int begin=0;
        //指针3
        int position=0;
        //最终过滤后结果
        StringBuilder sb=new StringBuilder();
        while (begin<text.length()){
            if (position<text.length()){
                Character c=text.charAt(position);
                if (isSymbol(c)){
                    if (tempNode==root){
                        begin++;
                        sb.append(c);
                    }
                    position++;
                    continue;
                }
                tempNode=tempNode.getSubNode(c);
                if (tempNode==null){
                    sb.append(text.charAt(begin));
                    position=++begin;
                    tempNode=root;
                }else if (tempNode.isKeywordEnd()){
                    sb.append(REPLACEMENT);
                    begin=++position;
                    tempNode=root;
                }else {
                    position++;
                }

            }else {
                sb.append(text.charAt(begin));
                position=++begin;
                tempNode=root;
            }

        }
        return sb.toString();


    }

    //判断是否为符号
    private boolean isSymbol(Character c){
        // 0x2E80~0x9FFF为东亚文字范围
        return !CharUtils.isAsciiAlphanumeric(c)&&(c<0x2E80||c>0x9FFF);
    }
}
