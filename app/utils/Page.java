package utils;

public class Page {
    public static final int DEFAULT_PAGE = 8;

    /**
     * imply if the page has previous page 上一页
     */
    private boolean hasPrePage;

    /**
     * imply if the page has next page 下一页
     */
    private boolean hasNextPage;

    /**
     * the number of every page 每一页显示多少条
     */
    private int everyPage;

    /**
     * the total page number,总页数
     */
    private int totalPage;

    /**
     * the number of current page 当前页
     */
    private int currentPage;

    /**
     * the begin index of the records by the current query 数据开始的索引
     */
    private int beginIndex;

    /**
     * the total records 总记录数
     */
    private int totalRecords;

    // 定义页面显示分页数的宽度,(即显示页码的数量)
    private int width = 7;
    // 定义显示页面的时候用户看到的起始分页
    private int begin = 0;
    // 定义显示页面的时候用户看到的结束分页
    private int end = 0;

    /**
     * The default constructor
     */
    public Page() {
        this.everyPage = DEFAULT_PAGE;
    }

    /**
     * construct the page by everyPage
     *
     * @param everyPage
     */
    public Page(int everyPage) {
        this.everyPage = everyPage;
    }

    /**
     * The whole constructor
     */
    public Page(boolean hasPrePage, boolean hasNextPage, int everyPage, int totalPage, int currentPage, int beginIndex, int totalRecords, int begin, int end) {
        this.hasPrePage = hasPrePage;
        this.hasNextPage = hasNextPage;
        this.everyPage = everyPage;
        this.totalPage = totalPage;
        this.currentPage = currentPage;
        this.beginIndex = beginIndex;
        this.totalRecords = totalRecords;
        this.begin = begin;
        this.end = end;
    }

    /**
     * @return Returns the beginIndex.
     */
    public int getBeginIndex() {
        return beginIndex;
    }

    /**
     * @param beginIndex The beginIndex to set.
     */
    public void setBeginIndex(int beginIndex) {
        this.beginIndex = beginIndex;
    }

    /**
     * @return Returns the currentPage.
     */
    public int getCurrentPage() {
        return currentPage;
    }

    /**
     * @param currentPage The currentPage to set.
     */
    public void setCurrentPage(int currentPage) {
        this.currentPage = currentPage;
    }

    /**
     * @return Returns the everyPage.
     */
    public int getEveryPage() {
        return everyPage;
    }

    /**
     * @param everyPage The everyPage to set.
     */
    public void setEveryPage(int everyPage) {
        this.everyPage = everyPage;
    }

    /**
     * @return Returns the hasNextPage.
     */
    public boolean getHasNextPage() {
        return hasNextPage;
    }

    /**
     * @param hasNextPage The hasNextPage to set.
     */
    public void setHasNextPage(boolean hasNextPage) {
        this.hasNextPage = hasNextPage;
    }

    /**
     * @return Returns the hasPrePage.
     */
    public boolean getHasPrePage() {
        return hasPrePage;
    }

    /**
     * @param hasPrePage The hasPrePage to set.
     */
    public void setHasPrePage(boolean hasPrePage) {
        this.hasPrePage = hasPrePage;
    }

    /**
     * @return Returns the totalPage.
     */
    public int getTotalPage() {
        return totalPage;
    }

    /**
     * @param totalPage The totalPage to set.
     */
    public void setTotalPage(int totalPage) {
        this.totalPage = totalPage;
    }

    /**
     * @param totalRecords The totalRecords to set.
     */
    public void setTotalRecords(int totalRecords) {
        this.totalRecords = totalRecords;
    }

    /**
     * @return Returns the totalRecords.
     */
    public int getTotalRecords() {
        return totalRecords;
    }

    public int getWidth() {
        return width;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public int getBegin() {
        return begin;
    }

    public void setBegin(int begin) {
        this.begin = begin;
    }

    public int getEnd() {
        return end;
    }

    public void setEnd(int end) {
        this.end = end;
    }
}
