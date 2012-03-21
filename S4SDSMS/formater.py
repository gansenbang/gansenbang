#!/usr/bin/python
# -*- coding: UTF-8 -*-
#参照sentencee.in格式，将原始测试数据文件转为json格式
#测试文件有4568800项
import os

s4_image = os.getenv('S4_IMAGE')

def makeJSON(s):
    items = s.split(',')
    l = []
    l.append('"zone":' + items[0])
    l.append('"rateVersion":' + items[1])
    l.append('"importSection":' + items[2])
    l.append('"importStation":' + items[3])
    l.append('"exportSection":' + items[4])
    l.append('"exportStation":' + items[5])
    l.append('"identifyingStation":' + items[6])
    l.append('"carType":' + items[7])
    l.append('"figure":' + items[8])
    l.append('"mileage":' + items[9])
    return '{' + ','.join(l) + '}'

source = open(s4_image + '/s4-example-testinput/rate', 'r')
result = open(s4_image + '/s4-example-testinput/rate.jin', 'w')
cnt = 0
for s in source:
    cnt += 1
    result.write(makeJSON(s.strip()) + '\n')
print cnt
