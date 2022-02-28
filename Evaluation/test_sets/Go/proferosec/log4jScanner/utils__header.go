package utils

import "github.com/pterm/pterm"

var (
	Version   string = "undefined"
	BuildTime string = "undefined"
)

func SetVersion(version, buildTime string) {
	Version = version
	BuildTime = buildTime
}

func PrintHeader() {
	// Generate BigLetters
	//pterm.DefaultCenter.WithCenterEachLineSeparately().Println("")
	//s, _ := pterm.DefaultBigText.WithLetters(pterm.NewLettersFromStringWithStyle("Profero", pterm.NewStyle(pterm.FgWhite))).Srender()
	s, _ := pterm.DefaultBigText.WithLetters(pterm.NewLettersFromStringWithStyle("Profero", pterm.NewStyle(pterm.FgLightWhite, pterm.BgBlack))).Srender()
	pterm.DefaultCenter.Print(s) // Print BigLetters with the default CenterPrinter
	pterm.DefaultCenter.WithCenterEachLineSeparately().Printf("log4jScanner version: %s", Version)
	pterm.DefaultCenter.WithCenterEachLineSeparately().Printf("contact@profero.io")
	pterm.DefaultCenter.WithCenterEachLineSeparately().Printf("https://github.com/proferosec/log4jScanner")
}
