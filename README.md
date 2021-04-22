# Ultrafast Local Outlier Detection from a Data Stream with Stationary Region Skipping

This is the implementation of the paper published in KDD 2020 [[Paper](https://drive.google.com/file/d/1iCw1QcQE1inY9sPiwPBXgXWQk2hsdFe5/view?usp=sharing)] [Slide([full](https://drive.google.com/file/d/11y7Gs703SKJBkPZ4nKKgua__dHXXMbkV/view?usp=sharing)/[poster](https://drive.google.com/file/d/1RayaXNrTpZigXu0PGPPEqT0fuy8pkK4_/view?usp=sharing))]  [Video([full](https://youtu.be/UyfunKM9RUE)/[poster](https://youtu.be/Yl5Ah05X5eA)/[promotion](https://youtu.be/4YWtCd2y9CY))]

## 1. Overview
Real-time outlier detection from a data stream is an increasingly important problem, especially as sensor-generated data streams abound in many applications owing to the prevalence of IoT and emergence of digital twins. Several density-based approaches have been proposed to address this problem, but arguably none of them is fast enough to meet the performance demand of real applications. This paper is founded upon a novel observation that, in many regions of the data space, data distributions hardly change across window slides. We propose a new algorithm, abbr. STARE, which identifies local regions in which data distributions hardly change and then skips updating the densities in those regions-a notion called stationary region skipping. Two techniques, data distribution approximation and cumulative net-change-based skip, are employed to efficiently and effectively implement the notion. Extensive experiments using synthetic and real data streams as well as a case study show that STARE is several orders of magnitude faster than the existing algorithms while achieving comparable or higher accuracy.

## 2. Data Sets
| Name    | # data points  | # Dim    | Link           |
| :-----: | :------------: | :------: |:--------------:|
| YahooA1 | 95K            | 1        | [link](https://webscope.sandbox.yahoo.com/catalog.php?datatype=s&did=70) |
| YahooA2 | 142K           | 1        | [link](https://webscope.sandbox.yahoo.com/catalog.php?datatype=s&did=70) |
| HTTP    | 567K           | 3        | [link](http://kdd.ics.uci.edu/databases/kddcup99/kddcup99.html) |
| DLR     | 23K            | 9        | [link](https://www.dlr.de/kn/en/desktopdefault.aspx/tabid-8500/14564_read-36508/) |
| ECG     | 112K           | 32       | [link](https://github.com/yuhang-lin/ECGAD_extended_result/) |

YahooA1, YahooA2, HTTP, and DLR data sets are included in this repository.

The new data set in CSV filetype can be added to "src/datasets/" to be evaluated by the agorithm.

The data set contains a set of lines, where each line "attr1,attr2,attr3,....attrN,label(0 or 1)" represents a data point.

The ID of a data point is simply the order of it in the data set.

## 3. Configuration
STARE algorithm was implemented in JAVA and run on **JDK 1.8.0.**
- Compile
```
cd src
javac test/simulator.java
```

## 4. How to run
- Parameter options
```
--D: the name of a CSV file (string)
--W: the size of a window (integer)
--S: the size of a slide (integer)
--R: the size of a grid cell (double)
--K: the number of neighbors (integer) 
--T: the skip threshold in [0,1] (double, default: 0.1)
--N: top-"N" outliers to report in a window (integer, default: the number of ground truth outliers)
--nW: the number of windows to process (integer, default: the maximum number of windows in the data set or 10000)
--P: the way of printing the result. "Console" or "File". (string, default: Console)
--O: whether to print outliers IDs or not. "1" (true) or "0" (false). (integer, default: 0(false))
```

- Example 1 (the unspecified parameters are set as the default values)
```
cd src
java test.simulator --D DLR --W 1000 --S 50 --R 18.8 --K 2

Dataset: DLR	W: 1000	S: 50	R: 18.8	K: 2	skipThred: 0.1	=>	RP: 0.722	AP: 0.604	avgCPUTime: 0.244	PeakMem: 3.0
```

- Example 2 (all parameters are specified)
```
cd src
java test.simulator --D YahooA1 --R 60 --K 140 --W 1415 --S 71  --T 0.1 --N -1 --nW 10000 --P File --O 1
cat Result_YahooA1.txt
...
At window 1316, detected top-N outliers IDs: 93434 93435 93433 93430 93431 93436 93432 93429 93367 93396 
At window 1317, detected top-N outliers IDs: 93436 
At window 1318, detected top-N outliers IDs: 94828 94847 94840 94852 94838 94841 94851 94839 94837 94850 94845 94842 94844 94846 94848 94827 94853 
Dataset: YahooA1	W: 1415	S: 71	R: 60.0	K: 140	skipThred: 0.1	=>	RP: 0.464	AP: 0.435	avgCPUTime: 6.521	PeakMem: 4.92
```

## 5. Citation
```
@inproceedings{yoon2020ultrafast,
  title={Ultrafast Local Outlier Detection from a Data Stream with Stationary Region Skipping},
  author={Yoon, Susik and Lee, Jae-Gil and Lee, Byung Suk},
  booktitle={Proceedings of the 26th ACM SIGKDD International Conference on Knowledge Discovery \& Data Mining},
  pages={1181--1191},
  year={2020}
}
```
