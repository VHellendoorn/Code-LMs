import fs from "fs";
import {copy} from "../src/util/util";

const examples = {};
function loadExample (path, sub) {
  let collection = examples;
  if (sub ) {
    examples[sub] = examples[sub] || {};
    collection = examples[sub];
  }

  fs.readdirSync(__dirname + `/examples${path}`)
    .filter(filename => filename.indexOf(".json") >= 0)
    .forEach(filename => {

    let example = JSON.parse(fs.readFileSync(__dirname + `/examples${path}/` + filename));
    if (example.data) {
      example.sSpec.data.find(dataObj => dataObj.name === "source_0").values = copy(example.data);
      example.eSpec.data.find(dataObj => dataObj.name === "source_0").values = copy(example.data);
    }

    collection[filename.replace(".json","")] = example;
  });
}

loadExample("/transition");
loadExample("/sequence", "sequence")
export default examples