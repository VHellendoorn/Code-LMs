import { default as vl2vg4gemini } from "../../../src/util/vl2vg4gemini";
import { default as EXAMPLES } from "../../exampleLoader";
import {
  recommendForSeq,
  canRecommendKeyframes,
  splitStagesPerTransition,
  recommendWithPath
} from "../../../src/recommender/sequence/index.js";

describe("recommendForSeq", () => {
  test("should recommend gemini specs for the given sequence", async () => {
    let {sequence, opt} = EXAMPLES.sequence.filter_aggregate;
    opt = {...opt, stageN: 3};
    let recommendations = await recommendForSeq(sequence.map(vl2vg4gemini), opt);
    let topRecomSpecs = recommendations[0].specs;

    expect(recommendations[0].cost).toBeLessThan(recommendations[1].cost)
    expect(topRecomSpecs.reduce((dur, recom) => {
      return dur + recom.spec.totalDuration
    }, 0))
      .toEqual(opt.totalDuration);

    expect(topRecomSpecs.reduce((dur, recom) => {
      return dur + recom.spec.timeline.concat.length
    }, 0))
      .toEqual(opt.stageN);
  })

  test("should recommend gemini specs for the sequence adding Y and aggregating", async () => {
    const {sequence, opt} = EXAMPLES.sequence.addY_aggregate_scale;

    let recommendations = await recommendForSeq(
      sequence.map(vl2vg4gemini),
      {...opt, stageN: 2}
    );
    let topRecom = recommendations[0];

    expect(recommendations.length).toEqual(1);
    expect(topRecom.specs[0].spec.totalDuration).toEqual(opt.totalDuration/2)
    expect(topRecom.specs[0].spec.timeline.concat.length).toEqual(1)

  })
})

describe("canRecommendKeyframes", () => {

  test("should return an error if the given charts are invalid VL charts.", async () => {
    const {start, end} = EXAMPLES.sequence.filter_aggregate;

    expect(canRecommendKeyframes({ hconcat: [ {mark: "point", encode: {x: {field: "X"}}}] }, end))
      .toMatchObject({reason: "Gemini++ cannot recommend keyframes for the given Vega-Lite charts."});

  })

})

describe("splitStagesPerTransition", () => {
  test("should return all possible splits.", () => {
    expect(splitStagesPerTransition(3,2).length).toBe(2);
    expect(splitStagesPerTransition(4,2).length).toBe(3);
    expect(splitStagesPerTransition(4,3).length).toBe(3);

  })
})

describe("recommendWithPath", () => {
  test("should recommend with paths for the given transition and stageN(=1).", async () => {
    let {start, end, opt} = EXAMPLES.sequence.filter_aggregate;
    opt = {...opt, stageN: 1};
    let recommendations = await recommendWithPath(start, end, opt);
    expect(recommendations['2']).toEqual(undefined);
    expect(recommendations['1'].length).toEqual(1);
    expect(recommendations['1'][0].path.sequence.length).toEqual(2);
    expect(recommendations['1'][0].recommendations.length).toEqual(1);
  })

  test("should recommend with paths for the given transition and stageN(=2).", async () => {
    let {start, end, opt} = EXAMPLES.sequence.filter_aggregate;
    opt = {...opt, stageN: 2};
    let recommendations = await recommendWithPath(start, end, opt);
    expect(recommendations['3']).toEqual(undefined);
    expect(recommendations['1'].length).toEqual(1);
    expect(recommendations['1'][0].path.sequence.length).toEqual(2);
    expect(recommendations['1'][0].recommendations[0].specs.length).toEqual(1);
    expect(recommendations['1'][0].recommendations[0].specs[0].spec.timeline.concat.length).toEqual(2);
    expect(recommendations['2'][0].recommendations[0].specs.length).toEqual(2);
    expect(recommendations['2'][0].recommendations[0].specs[0].spec.timeline.concat.length).toEqual(1);
  })
})
