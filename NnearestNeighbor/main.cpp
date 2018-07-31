//
//  main.cpp
//  NnearestNeighbor
//
//  Created by Hua Jiang on 4/22/15.
//  Copyright (c) 2015 Hua Jiang. All rights reserved.
//

#include <iostream>
#include <fstream>
#include <vector>
#include <unordered_map>
using namespace std;

// Type of glass maps
string glassMaps[] = {  "building_windows_float_processed",
                        "building_windows_non_float_processed",
                        "vehicle_windows_float_processed",
                        "vehicle_windows_non_float_processed (none in this database)",
                        "containers",
                        "tableware",
                        "headlamps"
};

// Wrapper class of glass 9 attributes
// Helper functions
class Glass {
public:
    vector<float> attributes;
    Glass() {}
    Glass(vector<float> &attrs) {
        attributes = attrs;
    }
    void addAttribute(float a){
        attributes.push_back(a);
    }
    void removeID() {
        // read redundant id from input file
        attributes.erase(attributes.begin());
    }
    
    // after specifying set, remove last set id
    int removeType() {
        int type = attributes.back();
        attributes.erase(attributes.end()-1);
        return type;
    }
    void printGlassAttrs() {
        for (float a : attributes) {
            cout << a << " ";
        }
        cout << endl;
    }
};
// typeid map to a vector of Glass instances
unordered_map<int, vector<Glass>> glassSets;
// Print type id with all glass attributes
void printGlassSets() {
    for (pair<int, vector<Glass>> p : glassSets) {
        cout << p.first << ": ";
        for (Glass g : p.second) {
            cout << "\t";
            g.printGlassAttrs();
        }
    }
}

/*
 Load file from file path.
 Get each glass attributes stored;
 Save each glass into glassSets by type id.
 */
void loadFile(string path) {
    ifstream myfile (path);
    string line;
    if (myfile.is_open()) {
        while (getline(myfile, line)) {
            //cout << line << endl;
            Glass g;
            char *charStr = (char *)line.c_str();
            char *token = strtok(charStr, ",");
            while (token != NULL) {
                g.addAttribute(atof(token));
                //cout << token << "+";
                token = strtok(NULL, ",");
            }
            g.removeID(); // remove first id
            int type = g.removeType(); // remove last set id
            //g.printGlassAttrs();
            
            if (glassSets.find(type) == glassSets.end()) // new set type
                glassSets[type] = {g};
            else
                glassSets[type].push_back(g); // append to the type
        }
        myfile.close();
    }
    
    //printGlassSets();
}

double calDistance(Glass &g1, Glass &g2) {
    int n = (int)g1.attributes.size();
    double sum = 0;
    for (int i=0; i<n; i++)
        sum += pow(g1.attributes[i]-g2.attributes[i], 2);
    
    return sqrt(sum);
}

// K nearest neighbors algorithm
// Find the shortest average distance among those to each set glasses
void classify(Glass &g) {
    //unordered_map<int, double> distances; // type id maps to shortest average distance
    double shortest = INT_MAX; // shortest distance to different set glasses
    int shortestType = -1;
    
    for (pair<int, vector<Glass>> p : glassSets) {
        int count = 0;
        double totalDistance = 0;
        for (Glass glass : p.second) {
            count++;
            totalDistance += calDistance(glass, g);
        }
        double aveDis = totalDistance/count;
        if (shortest > aveDis) {
            shortest = aveDis;
            shortestType = p.first;
        }
       // distances[p.first] = aveDis;
    }
//    for (pair<int, double> p : distances) {
//        cout << p.first << "=>" << p.second << endl;
//    }
    
    cout << "[" << shortestType << "]: " << glassMaps[shortestType-1] << endl;
}

Glass parseNewGlass(string attrs) {
    Glass g;
    char *charStr = (char *)attrs.c_str();
    char *token = strtok(charStr, ",");
    while (token != NULL) {
        g.addAttribute(atof(token));
        //cout << token << "+";
        token = strtok(NULL, ",");
    }
    return g;
}

/*
 (A) 1.52211,14.19,3.78,0.91,71.36,0.23,9.14,0.00,0.37 ==> 3 (vehicle_windows_float_processed)
 (B) 1.51514,14.01,2.68,3.50,69.89,1.68,5.87,2.20,0.00 ==> 3 (vehicle_windows_float_processed)
 (C) 1.51915,12.73,1.85,1.86,72.69,0.60,10.09,0.00,0.00 => 1 (building_windows_float_processed)
 */
int main(int argc, const char * argv[]) {
    if (argc < 2) {
        cerr << "Please specify the training set file path.\n";
        exit(1);
    }
    string path = argv[1];
    loadFile(path);
    //printGlassSets();
    
    while (1) {
        cout << "Please enter the testing glass attributes: ";
        string input;
        getline(cin, input);
        if (input == "q") {
            break; // quit
        }
        //cout << "Read: " << input << endl;
        Glass test = parseNewGlass(input);
        //test.printGlassAttrs();
        classify(test);
    }
    
    
    return 0;
}
