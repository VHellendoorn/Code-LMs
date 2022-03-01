import { vegaConfig, vegaConfig as vgConfig } from "../vegaConfig";
import * as util from "../../util/util"
const EMPTY_ENCODE = {
  enter: { opacity: { value: 0 } },
  exit: { opacity: { value: 0 } },
  update: { }
};


function axisCompPos(spec) {
  if (!spec) {
    return {};
  }
  const scName = spec.scale;
  if (spec.orient === "left" || spec.orient === "right") {
    return { y: { scale: scName, field: "value", band: 0.5 } };
  }
  return { x: { scale: scName, field: "value", band: 0.5 } };
}
function legendBandPos(spec) {
  let grLength = spec ? spec.gradientLength : undefined;
  let grThickness = spec ? spec.gradientThickness : undefined;
  grLength = grLength || vgConfig.legend.gradientLength;
  grThickness = grThickness || vgConfig.legend.gradientThickness;

  if (spec && spec.direction === "horizontal") {
    return {
      x: { signal: "(1-datum.perc)", mult: grLength },
      x2: { signal: "(1-datum.perc2)", mult: grLength },
      y: { value: 0 },
      height: { value: grThickness }
    };
  }
  return {
    y: { signal: "(1-datum.perc)", mult: grLength },
    y2: { signal: "(1-datum.perc2)", mult: grLength },
    x: { value: 0 },
    width: { value: grThickness }
  };
}

function legendLablePos(spec) {
  const columns = !spec ? 1 : (spec.columns || (spec.direction === "vertical" ? 1 : 0))
  const clipHeight = (spec && spec.clipHeight) ? spec.clipHeight : null;

  if (spec.type === "symbol") {
    return {
      x: { signal: columns ? `datum['offset']` : `datum['size']`, offset: vgConfig.legend.labelOffset },
      y: { signal: clipHeight ? `${clipHeight}` : `datum['size']`, mult: 0.5}
    };
  } else {
    let grLength = spec ? spec.gradientLength : undefined;
    grLength = isNaN(grLength) ? vgConfig.legend.gradientLength : grLength;
    let grThickness = spec ? spec.gradientThickness : undefined;
    grThickness = isNaN(grThickness) ? vgConfig.legend.gradientThickness : grThickness;
    let grLabelOffset = spec ? spec.gradientLabelOffset : undefined;
    grLabelOffset = isNaN(grLabelOffset) ? vgConfig.legend.gradientLabelOffset : grLabelOffset;

    if (spec.direction === "vertical") {
      return {
        x: { value: 0, },
        y: { signal: `(1-datum.perc) * clamp(height, 64, ${grLength})` },
        dx: { value: grThickness + grLabelOffset }
      };
    }
    return {
      x: { signal: `(datum.perc) * clamp(width, 64, ${grLength})` },
      y: { value: 0, },
      dy: { value: grThickness + grLabelOffset }
    };
  }

}


function titlePos(orient) {
  if (orient === "top") {
    return {x: { signal: "width", mult: 0.5}, y: {value: -22}};
  } if (orient === "bottom") {
    return {x: { signal: "width", mult: 0.5}, y: {value: 22}};
  } if (orient === "right") {
    return {y: { signal: "height", mult: 0.5}, x: {value: 22}};
  }
  return {y: { signal: "height", mult: 0.5}, x: {value: -22}};

}
function titleAngle(orient) {
  if (orient === "left") {
    return 270;
  }
  if (orient === "right") {
    return 90;
  }
  return 0;
}
function baseline(orient) {
  if (orient === "top") {
    return "bottom";
  }
  if (orient === "bottom") {
    return "top";
  }
  return "middle";
}
function tickLength(attr, orient) {
  if (attr === "x2") {
    if (orient === "right") {
      return vgConfig.axis.tickSize;
    }
    if (orient === "left") {
      return -vgConfig.axis.tickSize;
    }
  } else if (attr === "y2") {
    if (orient === "bottom") {
      return vgConfig.axis.tickSize;
    }
    if (orient === "top") {
      return -vgConfig.axis.tickSize;
    }
  }
  return 0;
}

function axisLabelAlign(spec) {
  if (spec && spec.labelAlign) {
    return spec.labelAlign;
  }

  if (spec && spec.orient === "right") {
    return "left";
  }
  if (spec && spec.orient === "left") {
    return "right";
  }
  return "center";
}

function legendLabelAlign(spec) {
  if (spec && spec.labelAlign) {
    return spec.labelAlign;
  }

  if (spec && spec.orient === "right") {
    return "left";
  }
  if (spec && spec.orient === "left") {
    return "right";
  }
  return "center";
}

function lableAngle(orient, scaleType) {
  if (orient === "top" || orient === "bottom") {
    if (["band", "point"].indexOf(scaleType) >= 0) {
      return 270;
    }
  }
  return 0;
}
function axisTextDpos(attr, spec) {
  const orient = spec ? spec.orient : undefined;
  const posOffset = (spec.ticks !== false ?
      ( util.isNumber(spec.tickSize) ? spec.tickSize : vegaConfig.axis.tickSize)
       : 0)
    + (util.isNumber(spec.labelPadding) ? spec.labelPadding : vegaConfig.axis.labelPadding);


  if (attr === "dx") {
    if (orient === "right") {
      return posOffset;
    }
    if (orient === "left") {
      return -posOffset;
    }
  } else if (attr === "dy") {
    if (orient === "bottom") {
      return posOffset;
    }
    if (orient === "top") {
      return - posOffset;
    }
  }
  return 0;
}

function gridLength(orient, gridScale){
  if (orient === "bottom" || orient === "top") {
    if (!gridScale){
      return {
        y2: {signal: "height", mult: -1},
        y: {value: 0},
        x2: {value: 0}
      };
    }
    return {
      // y2: {signal: "height"},
      // y: {signal: "height", mult: -1},
      y2: {signal: "height", mult: -1},
      y: {value: 0},
      x2: {value: 0}
    };

  }

  if (orient === "right" || orient === "left") {
    if (!gridScale){
      return {
        x2: {signal: "width"},
        x: {value: 0},
        y2: {value: 0}
      };
    }
    return {
      x2: {signal: "width"},
      x: {value: 0},
      y2: {value: 0}
    };

  }

}

function domainLength(orient) {
  if (orient === "bottom" || orient === "top") {
    return {
      y2: { value: 0 },
      x2: { signal: "width" },
      y: { value: 0 },
      x: { value: 0 }
    };
  }
  if (orient === "right" || orient === "left") {
    return {
      y2: { signal: "height", mult: 1 },
      x2: { value: 0 },
      y: { value: 0 },
      x: { value: 0 }
    };
  }
  return {
    y2: { value: 0 },
    x2: { value: 0 }
  };
}

export {

  legendBandPos,
  legendLablePos,
  legendLabelAlign,

  titlePos,
  titleAngle,
  baseline,

  axisCompPos,
  lableAngle,
  tickLength,
  axisLabelAlign,
  axisTextDpos,
  gridLength,
  domainLength,

  EMPTY_ENCODE
};