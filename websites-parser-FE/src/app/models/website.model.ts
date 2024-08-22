export class Website {
    private allPagesHtml: string[] = [];
    private information: Map<string, string[]> = new Map<string, string[]>();

  public getAllPagesHtml(): string[] {
    return this.allPagesHtml;
  }

  public setAllPagesHtml(value: string[]): void {
      this.allPagesHtml = value;
  }

  public getInformation(): Map<string, string[]> {
    return this.information;
  }

  public setInformation(key: string, value: string[]): void {
      this.information.set(key, value);
  }

}
