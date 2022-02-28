import axios from "axios";
import chalk from "chalk";
import moment from "moment";
import { flags } from "@oclif/command";
// tslint:disable-next-line: no-implicit-dependencies
import { CLIError } from "@oclif/errors";

import Command from "../../base";
import { createDebugLogger } from "../../utils/output";

interface ILog {
  type: string;
  datetime: string;
  message: string;
}

export default class AppLogs extends Command {
  static description = "fetch the logs of an app";

  static flags = {
    ...Command.flags,
    app: flags.string({ char: "a", description: "app id" }),
    since: flags.integer({
      char: "s",
      description: "show logs since timestamp",
    }),
  };

  static aliases = ["logs"];

  async run() {
    const { flags } = this.parse(AppLogs);
    let since: string | number = flags.since || 1;

    this.debug = createDebugLogger(flags.debug);

    this.setAxiosConfig({
      ...this.readGlobalConfig(),
      ...flags,
    });

    const project = flags.app || (await this.promptProject());

    setInterval(async () => {
      this.debug("Polling...");

      let logs: ILog[] = [];

      try {
        const { data } = await axios.get<ILog[]>(
          `/v1/projects/${project}/logs?since=${since}`,
          {
            ...this.axiosConfig,
          }
        );

        logs = data;
      } catch (error) {
        if (error.response && error.response.status === 404) {
          // tslint:disable-next-line: no-console
          console.error(new CLIError("App not found.").render());
          process.exit(2);
        }

        this.debug(error.stack);
      }

      const lastLog = logs[logs.length - 1];

      if (lastLog && lastLog.datetime === "Error") {
        // tslint:disable-next-line: no-console
        console.error(
          new CLIError(`${lastLog.message}
Sorry for inconvenience. Please contact us.`).render()
        );
        process.exit(1);
      }

      if (lastLog) {
        since = moment(lastLog.datetime).unix() + 1;
      }

      for (const log of logs) {
        const datetime = chalk.gray(
          moment(log.datetime).format("YYYY-MM-DD HH:mm:ss")
        );
        this.log(`${datetime} | ${log.message}`);
      }
    }, 1000);
  }
}
